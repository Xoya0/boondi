package com.boondi.application.service;

import com.boondi.application.dto.request.ResetPasswordRequest;
import com.boondi.domain.entity.PasswordResetToken;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.PasswordResetTokenRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Initiates a password reset. Always returns success to avoid email enumeration.
     */
    @Transactional
    public void sendPasswordResetEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            // Remove any existing unused tokens
            resetTokenRepository.deleteByUserIdAndIsUsedFalse(user.getId());

            String rawToken = UUID.randomUUID().toString().replace("-", "") +
                              UUID.randomUUID().toString().replace("-", "");
            String tokenHash = sha256Hex(rawToken);

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(tokenHash)
                    .expiresAt(OffsetDateTime.now().plusHours(1))
                    .build();

            resetTokenRepository.save(resetToken);

            String resetLink = baseUrl + "/reset-password?token=" + rawToken;
            String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
            emailService.sendPasswordResetEmail(user.getEmail(), displayName, resetLink);

            log.info("Password reset email dispatched for userId={}", user.getId());
        });
    }

    /**
     * Resets the password using the token from the email link.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256Hex(request.getToken());

        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(BoondiException::resetTokenInvalid);

        if (resetToken.isUsed()) {
            throw BoondiException.resetTokenInvalid();
        }
        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw BoondiException.resetTokenExpired();
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetTokenRepository.save(resetToken);

        // Invalidate any active refresh token for this user
        redisTemplate.delete("refresh:" + user.getId());

        log.info("Password reset completed for userId={}", user.getId());
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
