package com.spawnta.admin.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.spawnta.repository.UserRepository;

@Component
public class SuspendedUserReactivationScheduler {

    private static final Logger log = LoggerFactory.getLogger(SuspendedUserReactivationScheduler.class);

    private final UserRepository userRepository;

    public SuspendedUserReactivationScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 15 * * * *")
    @Transactional
    public void reactivateExpiredSuspensions() {
        var expiredSuspensions = userRepository.findExpiredSuspensions(LocalDateTime.now());
        expiredSuspensions.forEach(user -> {
            user.setSuspendedUntil(null);
            user.setSuspensionReason(null);
        });
        if (!expiredSuspensions.isEmpty()) {
            userRepository.saveAll(expiredSuspensions);
            log.info("Reactivated {} users with expired suspensions", expiredSuspensions.size());
        }
    }
}
