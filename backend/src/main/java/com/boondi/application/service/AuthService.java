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
import com.boondi.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RedisTemplate<String, String> redisTemplate;

    // @Lazy to break potential circular dependency through Spring context
    @Lazy
    @Autowired
    private EmailVerificationService emailVerificationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw BoondiException.emailAlreadyExists();
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw BoondiException.usernameAlreadyExists();
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null
                        ? request.getDisplayName()
                        : request.getUsername())
                .role(UserRole.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: id={}, username={}", saved.getId(), saved.getUsername());

        // Send verification email asynchronously (won't block the response)
        emailVerificationService.sendVerificationEmail(saved);

        return generateTokens(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(BoondiException::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BoondiException.invalidCredentials();
        }

        if (user.isSuspended()) {
            throw BoondiException.accountSuspended();
        }

        log.info("User logged in: id={}, username={}", user.getId(), user.getUsername());
        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Store refresh token in Redis: key = refresh:{userId}
        String redisKey = "refresh:" + user.getId().toString();
        long refreshTtlSeconds = jwtTokenProvider.getRefreshTokenExpiryMs() / 1000;
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTtlSeconds, TimeUnit.SECONDS);

        UserResponse userResponse = userMapper.toResponse(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .user(userResponse)
                .build();
    }
}
