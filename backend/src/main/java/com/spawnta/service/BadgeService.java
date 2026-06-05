package com.spawnta.service;

import com.spawnta.entity.*;
import com.spawnta.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    private static final Logger log = LoggerFactory.getLogger(BadgeService.class);

    private final UserRepository userRepository;
    private final BadgeRepository badgeRepository;
    private final UserAchievementRepository achievementRepository;
    private final ActivityAttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;
    private final GamificationService gamificationService;

    public BadgeService(UserRepository userRepository,
                        BadgeRepository badgeRepository,
                        UserAchievementRepository achievementRepository,
                        ActivityAttendanceRepository attendanceRepository,
                        ActivityRepository activityRepository,
                        @Lazy GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.badgeRepository = badgeRepository;
        this.achievementRepository = achievementRepository;
        this.attendanceRepository = attendanceRepository;
        this.activityRepository = activityRepository;
        this.gamificationService = gamificationService;
    }

    @Transactional(readOnly = true)
    public List<Badge> listAvailableBadges() {
        return badgeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Badge> getAllUserAchievements(Long userId) {
        return achievementRepository.findByUserId(userId).stream()
                .map(UserAchievement::getBadge)
                .collect(Collectors.toList());
    }

    @Transactional
    public void awardBadge(Long userId, String badgeName) {
        Badge badge = badgeRepository.findByName(badgeName)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found with name: " + badgeName));

        boolean alreadyEarned = achievementRepository.existsByUserIdAndBadgeId(userId, badge.getId());
        if (alreadyEarned) {
            return;
        }

        log.info("Awarding badge '{}' to user ID: {}", badgeName, userId);

        UserAchievement achievement = new UserAchievement(userId, badge.getId());
        achievementRepository.save(achievement);

        if (badge.getXpReward() != null && badge.getXpReward() > 0) {
            // Award reward XP
            gamificationService.awardXp(userId, badge.getXpReward(), "Earned Badge: " + badgeName);
        }
    }

    @Transactional
    public void checkAttendanceBadges(Long userId) {
        long confirmedCount = attendanceRepository.countByParticipantIdAndStatus(userId, AttendanceStatus.CONFIRMED);

        // 1. Explorer (1 confirmed activity completed)
        if (confirmedCount >= 1) awardBadge(userId, "Explorer");

        // 2. Social Butterfly (5 confirmed activities)
        if (confirmedCount >= 5) awardBadge(userId, "Social Butterfly");

        // 3. Reliable (10 confirmed activities)
        if (confirmedCount >= 10) awardBadge(userId, "Reliable");

        // 4. Trailblazer (20 confirmed activities)
        if (confirmedCount >= 20) awardBadge(userId, "Trailblazer");
    }

    @Transactional
    public void checkHostingBadges(Long userId) {
        long hostedCount = activityRepository.countByHostId(userId);

        // 5. Activity Master (Host 5 activities)
        if (hostedCount >= 5) awardBadge(userId, "Activity Master");

        // 6. Community Leader (Host 15 activities)
        if (hostedCount >= 15) awardBadge(userId, "Community Leader");
    }

    @Transactional
    public void checkLevelBadges(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // 7. Adventurer (Reach level 10)
        if (user.getLevel() >= 10) awardBadge(userId, "Adventurer");

        // 8. Veteran (Reach level 25)
        if (user.getLevel() >= 25) awardBadge(userId, "Veteran");
    }

    @Transactional
    public void checkBadgeCriteria(Long userId) {
        checkAttendanceBadges(userId);
        checkHostingBadges(userId);
        checkLevelBadges(userId);
    }
}
