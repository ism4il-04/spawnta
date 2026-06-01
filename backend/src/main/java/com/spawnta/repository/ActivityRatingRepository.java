package com.spawnta.repository;

import com.spawnta.entity.ActivityRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRatingRepository extends JpaRepository<ActivityRating, Long> {
    Optional<ActivityRating> findByActivityIdAndRaterId(Long activityId, Long raterId);
    List<ActivityRating> findByActivityId(Long activityId);
    List<ActivityRating> findByRaterId(Long raterId);

    @Query("SELECT r FROM ActivityRating r JOIN FETCH r.rater WHERE r.activity.id = :activityId ORDER BY r.createdAt DESC")
    List<ActivityRating> findByActivityIdWithRater(@Param("activityId") Long activityId);
}
