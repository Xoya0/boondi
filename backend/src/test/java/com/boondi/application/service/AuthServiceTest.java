package com.boondi.application.service;

import com.boondi.application.dto.request.LoginRequest;
import com.boondi.application.dto.request.RegisterRequest;
import com.boondi.application.dto.response.AuthResponse;
import com.boondi.application.dto.response.UserResponse;
import com.boondi.application.mapper.UserMapper;
import com.boondi.domain.entity.User;
import com.boondi.domain.enums.UserRole;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.exception.ErrorCode;
import com.boondi.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private UserMapper userMapper;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        // emailVerificationService is field-injected (@Lazy @Autowired), which Mockito's
        // constructor injection doesn't cover — wire it by hand.
        ReflectionTestUtils.setField(authService, "emailVerificationService", emailVerificationService);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(jwtTokenProvider.generateAccessToken(any(User.class))).thenReturn("access-token");
        lenient().when(jwtTokenProvider.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        lenient().when(jwtTokenProvider.getAccessTokenExpiryMs()).thenReturn(900_000L);
        lenient().when(jwtTokenProvider.getRefreshTokenExpiryMs()).thenReturn(604_800_000L);
        lenient().when(userMapper.toResponse(any(User.class))).thenReturn(mock(UserResponse.class));
    }

    private RegisterRequest registerRequest() {
        return RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .build();
    }

    @Test
    void register_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_rejectsDuplicateUsername() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS));
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_hashesPassword_defaultsDisplayNameToUsername_andIssuesTokens() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        AuthResponse response = authService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getDisplayName()).isEqualTo("john_doe");
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        verify(emailVerificationService).sendVerificationEmail(saved);
        // Refresh token is persisted server-side so logout/refresh-rotation can revoke it.
        verify(valueOperations).set(eq("refresh:" + saved.getId()), anyString(), anyLong(), any());
    }

    @Test
    void login_rejectsUnknownEmail_withCredentialErrorNotUserNotFound() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // INVALID_CREDENTIALS (not USER_NOT_FOUND) so login can't be used to enumerate accounts.
        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost@example.com", "pw")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_rejectsWrongPassword() {
        User user = User.builder().email("john@example.com").passwordHash("hashed").build();
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@example.com", "wrong")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void login_rejectsSuspendedAccount() {
        User user = User.builder().email("john@example.com").passwordHash("hashed").build();
        user.setSuspended(true);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("john@example.com", "password123")))
                .isInstanceOfSatisfying(BoondiException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_SUSPENDED));
    }

    @Test
    void login_returnsTokensOnSuccess() {
        User user = User.builder().email("john@example.com").passwordHash("hashed").build();
        user.setId(UUID.randomUUID());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        AuthResponse response = authService.login(new LoginRequest("john@example.com", "password123"));

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo(900L);
    }
}
