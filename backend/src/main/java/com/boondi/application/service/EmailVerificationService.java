package com.boondi.application.service;

import com.boondi.domain.entity.EmailVerification;
import com.boondi.domain.entity.User;
import com.boondi.domain.repository.EmailVerificationRepository;
import com.boondi.domain.repository.UserRepository;
import com.boondi.infrastructure.exception.BoondiException;
import com.boondi.infrastructure.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Generates a verification token and sends the verification email.
     * Called asynchronously after registration.
     */
    @Transactional
    public void sendVerificationEmail(User user) {
        if (user.isEmailVerified()) {
            return; // already verified, skip
        }

        // Remove any existing unused tokens for this user
        verificationRepository.deleteByUserIdAndIsUsedFalse(user.getId());

        // Generate raw token and hash for storage
        String rawToken = UUID.randomUUID().toString().replace("-", "") +
                          UUID.randomUUID().toString().replace("-", "");
        String tokenHash = sha256Hex(rawToken);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();

        verificationRepository.save(verification);

        String verificationLink = baseUrl + "/verify-email?token=" + rawToken;
        String displayName = user.getDisplayName() != null ? user.getDisplayName() : user.getUsername();
        emailService.sendVerificationEmail(user.getEmail(), displayName, verificationLink);

        log.info("Verification email dispatched for userId={}", user.getId());
    }

    /**
     * Verifies the email using the raw token from the email link.
     */
    @Transactional
    public void verifyEmail(String rawToken) {
        String tokenHash = sha256Hex(rawToken);

        EmailVerification verification = verificationRepository.findByTokenHash(tokenHash)
                .orElseThrow(BoondiException::verificationTokenInvalid);

        if (verification.isUsed()) {
            throw BoondiException.verificationTokenInvalid();
        }
        if (verification.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw BoondiException.verificationTokenExpired();
        }

        User user = verification.getUser();
        if (user.isEmailVerified()) {
            throw BoondiException.emailAlreadyVerified();
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        verification.setUsed(true);
        verificationRepository.save(verification);

        log.info("Email verified for userId={}", user.getId());
    }

    /**
     * Resends the verification email to the authenticated user.
     */
    @Transactional
    public void resendVerification(User user) {
        if (user.isEmailVerified()) {
            throw BoondiException.emailAlreadyVerified();
        }
        sendVerificationEmail(user);
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
