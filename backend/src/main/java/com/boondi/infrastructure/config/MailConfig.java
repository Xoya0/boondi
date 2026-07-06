package com.boondi.infrastructure.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Mail configuration is handled automatically by Spring Boot via spring.mail.* properties.
 * MailHog is used in development (localhost:1025).
 * This class exists as a placeholder for future customisation (e.g. MIME message factory).
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {
    // Spring Boot auto-configures JavaMailSender from spring.mail.* properties.
}
