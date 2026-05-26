package com.spawnta.controller;

import com.spawnta.dto.BadgeDto;
import com.spawnta.dto.GamificationProfileDto;
import com.spawnta.dto.LeaderboardEntryDto;
import com.spawnta.dto.LevelHistoryDto;
import com.spawnta.entity.Badge;
import com.spawnta.entity.User;
import com.spawnta.entity.UserAchievement;
import com.spawnta.entity.UserLevelHistory;
import com.spawnta.repository.UserLevelHistoryRepository;
import com.spawnta.repository.UserRepository;
import com.spawnta.repository.UserAchievementRepository;
import com.spawnta.service.BadgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gamification")
public class GamificationController {

    private final UserRepository userRepository;
    private final BadgeService badgeService;
    private final UserLevelHistoryRepository levelHistoryRepository;
    private final UserAchievementRepository achievementRepository;

    public GamificationController(UserRepository userRepository,
                                  BadgeService badgeService,
                                  UserLevelHistoryRepository levelHistoryRepository,
                                  UserAchievementRepository achievementRepository) {
        this.userRepository = userRepository;
        this.badgeService = badgeService;
        this.levelHistoryRepository = levelHistoryRepository;
        this.achievementRepository = achievementRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<GamificationProfileDto> getProfile(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Fetch Achievements
        List<UserAchievement> achievements = achievementRepository.findByUserId(user.getId());
        List<BadgeDto> badgeDtos = achievements.stream()
                .map(ua -> new BadgeDto(
                        ua.getBadge().getId(),
                        ua.getBadge().getName(),
                        ua.getBadge().getDescription(),
                        ua.getBadge().getIconUrl(),
                        ua.getBadge().getXpReward()
                ))
                .collect(Collectors.toList());

        // Fetch Level History
        List<UserLevelHistory> histories = levelHistoryRepository.findByUserIdOrderByAchievedAtDesc(user.getId());
        List<LevelHistoryDto> historyDtos = histories.stream()
                .map(h -> new LevelHistoryDto(
                        h.getId(),
                        h.getOldLevel(),
                        h.getNewLevel(),
                        h.getAchievedAt()
                ))
                .collect(Collectors.toList());

        GamificationProfileDto profileDto = new GamificationProfileDto(
                user.getLevel(),
                user.getXp(),
                user.getCurrentLevelXpRequired(),
                user.getTotalXpEarned(),
                badgeDtos,
                historyDtos
        );

        return ResponseEntity.ok(profileDto);
    }

    @GetMapping("/badges")
    public ResponseEntity<List<BadgeDto>> getBadges() {
        List<Badge> badges = badgeService.listAvailableBadges();
        List<BadgeDto> badgeDtos = badges.stream()
                .map(b -> new BadgeDto(
                        b.getId(),
                        b.getName(),
                        b.getDescription(),
                        b.getIconUrl(),
                        b.getXpReward()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(badgeDtos);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard() {
        List<User> topUsers = userRepository.findTop50ByOrderByLevelDescTotalXpEarnedDesc();
        List<LeaderboardEntryDto> leaderboard = topUsers.stream()
                .map(u -> new LeaderboardEntryDto(
                        u.getId(),
                        u.getFirstName() + " " + u.getLastName(),
                        u.getAvatarUrl(),
                        u.getLevel(),
                        u.getTotalXpEarned()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(leaderboard);
    }
}
