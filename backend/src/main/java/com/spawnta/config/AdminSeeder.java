package com.spawnta.config;

import com.spawnta.entity.Role;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures a default admin account exists on every startup.
 *
 * Behaviour:
 *  - If no user with ADMIN_EMAIL exists → create it (email pre-verified, role=ADMIN).
 *  - If the account exists but the stored password no longer matches ADMIN_PASSWORD
 *    (e.g. after a credential rotation) → update the hash automatically.
 *  - Skipped entirely when ADMIN_EMAIL or ADMIN_PASSWORD is blank/missing,
 *    so a mis-configured environment never overwrites a live account silently.
 *
 * Credentials are injected from environment variables:
 *   ADMIN_EMAIL     – login email for the admin console
 *   ADMIN_PASSWORD  – plain-text password (hashed with BCrypt before storage)
 *   ADMIN_FIRSTNAME – optional, defaults to "Admin"
 *   ADMIN_LASTNAME  – optional, defaults to "Spawnta"
 */
@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Value("${admin.firstname:Admin}")
    private String adminFirstName;

    @Value("${admin.lastname:Spawnta}")
    private String adminLastName;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("[AdminSeeder] ADMIN_EMAIL or ADMIN_PASSWORD not set – skipping admin seed.");
            return;
        }

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            existing -> syncPassword(existing),
            ()       -> createAdmin()
        );
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void createAdmin() {
        User admin = new User(
            adminEmail,
            passwordEncoder.encode(adminPassword),
            adminFirstName,
            adminLastName
        );
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);
        userRepository.save(admin);
        log.info("[AdminSeeder] Admin account created → {}", adminEmail);
    }

    private void syncPassword(User existing) {
        if (!passwordEncoder.matches(adminPassword, existing.getPassword())) {
            existing.setPassword(passwordEncoder.encode(adminPassword));
            // Ensure the account is never accidentally locked out
            existing.setEmailVerified(true);
            existing.setRole(Role.ADMIN);
            userRepository.save(existing);
            log.info("[AdminSeeder] Admin password updated for → {}", adminEmail);
        } else {
            log.debug("[AdminSeeder] Admin account already up-to-date → {}", adminEmail);
        }
    }
}
