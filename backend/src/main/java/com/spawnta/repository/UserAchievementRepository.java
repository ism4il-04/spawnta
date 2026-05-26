package com.spawnta.repository;

import com.spawnta.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UserAchievement.UserAchievementId> {
    List<UserAchievement> findByUserId(Long userId);
    boolean existsByUserIdAndBadgeId(Long userId, Integer badgeId);
}
