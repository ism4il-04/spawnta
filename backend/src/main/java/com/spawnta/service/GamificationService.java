package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.entity.OutboxEvent;
import com.spawnta.entity.User;
import com.spawnta.entity.UserLevelHistory;
import com.spawnta.repository.UserLevelHistoryRepository;
import com.spawnta.repository.UserRepository;
import com.spawnta.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationService.class);

    private final UserRepository userRepository;
    private final UserLevelHistoryRepository levelHistoryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final BadgeService badgeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GamificationService(UserRepository userRepository,
                               UserLevelHistoryRepository levelHistoryRepository,
                               OutboxEventRepository outboxEventRepository,
                               BadgeService badgeService) {
        this.userRepository = userRepository;
        this.levelHistoryRepository = levelHistoryRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.badgeService = badgeService;
    }

    /**
     * Calculates the XP required for the next level based on the formula: 1000 * (level ^ 1.2).
     */
    public int calculateNextLevelXp(int level) {
        return (int) (1000 * Math.pow(level, 1.2));
    }

    @Transactional
    public void awardXp(Long userId, int xpAmount, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        log.info("Awarding {} XP to user {} for reason: {}", xpAmount, user.getEmail(), reason);

        user.setXp(user.getXp() + xpAmount);
        user.setTotalXpEarned(user.getTotalXpEarned() + xpAmount);
        userRepository.save(user);

        checkLevelUp(user);
    }

    private void checkLevelUp(User user) {
        int originalLevel = user.getLevel();
        boolean leveledUp = false;

        while (user.getXp() >= user.getCurrentLevelXpRequired()) {
            int xpRequired = user.getCurrentLevelXpRequired();
            user.setXp(user.getXp() - xpRequired);
            user.setLevel(user.getLevel() + 1);
            user.setCurrentLevelXpRequired(calculateNextLevelXp(user.getLevel()));
            leveledUp = true;
        }

        if (leveledUp) {
            log.info("User {} leveled up from {} to {}", user.getEmail(), originalLevel, user.getLevel());

            // Save history
            UserLevelHistory history = new UserLevelHistory(user, originalLevel, user.getLevel(), user.getTotalXpEarned());
            levelHistoryRepository.save(history);

            // Save User state first
            userRepository.save(user);

            // Write transactional outbox event
            createLevelUpOutboxEvent(user, originalLevel, user.getLevel());

            // Trigger level-specific badge checks
            badgeService.checkLevelBadges(user.getId());
        }
    }

    private void createLevelUpOutboxEvent(User user, int oldLevel, int newLevel) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("userId", user.getId());
            payload.put("userEmail", user.getEmail());
            payload.put("oldLevel", oldLevel);
            payload.put("newLevel", newLevel);
            payload.put("action", "LEVEL_UP");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent("user.level_up", jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize level-up event to outbox", e);
        }
    }
}
