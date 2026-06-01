package com.spawnta.repository;

import com.spawnta.entity.ActivityRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityRecommendationRepository extends JpaRepository<ActivityRecommendation, Long> {
    Optional<ActivityRecommendation> findByUserIdAndActivityId(Long userId, Long activityId);
    List<ActivityRecommendation> findByUserIdOrderByScoreDesc(Long userId);
    List<ActivityRecommendation> findByUserIdAndClickedFalseOrderByScoreDesc(Long userId);
}
