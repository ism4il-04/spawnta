package com.spawnta.repository;

import com.spawnta.entity.ActivityRating;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityRatingRepository extends JpaRepository<ActivityRating, Long> {
    Optional<ActivityRating> findByActivityIdAndRaterId(Long activityId, Long raterId);
    List<ActivityRating> findByActivityId(Long activityId);
    List<ActivityRating> findByRaterId(Long raterId);
}
