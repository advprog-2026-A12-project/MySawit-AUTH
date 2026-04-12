package id.ac.ui.cs.advprog.auth.config;

import id.ac.ui.cs.advprog.auth.enums.UserRole;
import id.ac.ui.cs.advprog.auth.model.User;
import id.ac.ui.cs.advprog.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@mysawit.local";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_NAME = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.bootstrap.admin-initial-secret:}")
    private String adminInitialSecret;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        if (adminInitialSecret == null || adminInitialSecret.isBlank()) {
            log.warn("Skipping ADMIN seed because app.bootstrap.admin-initial-secret is missing");
            return;
        }

        String uniqueUsername = buildUniqueUsername(ADMIN_USERNAME);

        User admin = User.builder()
                .username(uniqueUsername)
                .email(ADMIN_EMAIL)
                .name(ADMIN_NAME)
                .passwordHash(passwordEncoder.encode(adminInitialSecret))
                .role(UserRole.ADMIN)
                .isActive(true)
                .build();

        userRepository.save(admin);
        log.info("Seeded default ADMIN account with email={}", ADMIN_EMAIL);
    }

    private String buildUniqueUsername(String preferredUsername) {
        if (!userRepository.existsByUsername(preferredUsername)) {
            return preferredUsername;
        }

        int suffix = 1;
        String candidate;
        do {
            candidate = preferredUsername + "-" + suffix;
            suffix++;
        } while (userRepository.existsByUsername(candidate));

        return candidate;
    }
}