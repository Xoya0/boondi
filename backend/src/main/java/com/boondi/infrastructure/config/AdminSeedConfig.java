package com.boondi.infrastructure.config;

import com.boondi.domain.enums.UserRole;
import com.boondi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * Promotes configured accounts to ADMIN at startup (E9 gap flagged in PROGRESS.md — there
 * was previously no way to get an admin without a manual {@code UPDATE users SET role} in
 * psql). Set {@code ADMIN_EMAILS} to a comma-separated list of registered account emails;
 * matching users are promoted on every boot (idempotent — already-admin users are skipped,
 * unknown emails are logged and skipped so a typo can't break startup). Demotion is
 * deliberately NOT automatic: removing an email from the list leaves the account admin,
 * because silently stripping privileges on a config change is more surprising than helpful.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminSeedConfig {

    private final UserRepository userRepository;

    @Value("${app.admin.emails:}")
    private String adminEmails;

    @Bean
    public ApplicationRunner adminSeeder() {
        return args -> promoteConfiguredAdmins();
    }

    @Transactional
    void promoteConfiguredAdmins() {
        List<String> emails = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        for (String email : emails) {
            userRepository.findByEmail(email).ifPresentOrElse(user -> {
                if (user.getRole() != UserRole.ADMIN) {
                    user.setRole(UserRole.ADMIN);
                    userRepository.save(user);
                    log.info("Promoted '{}' to ADMIN (app.admin.emails)", email);
                }
            }, () -> log.warn("app.admin.emails entry '{}' matches no registered user — skipped", email));
        }
    }
}
