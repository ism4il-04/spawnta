package com.spawnta.service;

import com.spawnta.repository.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MessageRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(MessageRetentionScheduler.class);

    private final MessageRepository messageRepository;

    public MessageRetentionScheduler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Runs daily at 2:00 AM
    @Transactional
    public void purgeExpiredMessages() {
        log.info("Starting daily chat message retention cleanup task...");

        // Purge group chat messages older than 2 years (Requirement 5.5)
        LocalDateTime groupCutoff = LocalDateTime.now().minusYears(2);
        int deletedGroups = messageRepository.deleteExpiredGroupMessages(groupCutoff);
        log.info("Purged {} expired group chat messages older than 2 years", deletedGroups);

        // Purge private chat messages older than 1 year (Requirement 5.5)
        LocalDateTime privateCutoff = LocalDateTime.now().minusYears(1);
        int deletedPrivates = messageRepository.deleteExpiredPrivateMessages(privateCutoff);
        log.info("Purged {} expired private chat messages older than 1 year", deletedPrivates);
    }
}
