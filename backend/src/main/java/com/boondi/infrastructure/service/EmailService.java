package com.boondi.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@boondi.app}")
    private String fromAddress;

    @Async
    public void sendVerificationEmail(String toEmail, String displayName, String verificationLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Verify your Boondi email address");
            message.setText("""
                    Hi %s,

                    Welcome to Boondi! Please verify your email address by clicking the link below:

                    %s

                    This link expires in 24 hours.

                    If you didn't create a Boondi account, you can safely ignore this email.

                    — The Boondi Team
                    """.formatted(displayName, verificationLink));
            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String displayName, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Reset your Boondi password");
            message.setText("""
                    Hi %s,

                    We received a request to reset your Boondi password. Click the link below to set a new password:

                    %s

                    This link expires in 1 hour.

                    If you didn't request a password reset, you can safely ignore this email. Your password will not change.

                    — The Boondi Team
                    """.formatted(displayName, resetLink));
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
        }
    }
}
