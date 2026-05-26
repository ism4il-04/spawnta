package com.spawnta.service;

import com.spawnta.dto.ActivityStatsDto;
import com.spawnta.entity.*;
import com.spawnta.repository.ActivityAttendanceRepository;
import com.spawnta.repository.ActivityRatingRepository;
import com.spawnta.repository.ActivityRepository;
import com.spawnta.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ActivityRatingService {

    private static final Logger log = LoggerFactory.getLogger(ActivityRatingService.class);

    private final ActivityRatingRepository ratingRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityAttendanceRepository attendanceRepository;

    public ActivityRatingService(ActivityRatingRepository ratingRepository,
                                 ActivityRepository activityRepository,
                                 UserRepository userRepository,
                                 ActivityAttendanceRepository attendanceRepository) {
        this.ratingRepository = ratingRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public ActivityRating rateActivity(Long activityId, Long userId, Integer score, String comment) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Integrity check: Only confirmed attendees can rate
        ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, userId)
                .orElse(null);

        if (attendance == null || attendance.getStatus() != AttendanceStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed activity attendees can rate or review this activity.");
        }

        log.info("User {} is rating activity {} with score: {}", user.getEmail(), activityId, score);

        ActivityRating rating = ratingRepository.findByActivityIdAndRaterId(activityId, userId)
                .orElseGet(() -> {
                    ActivityRating newRating = new ActivityRating();
                    newRating.setActivity(activity);
                    newRating.setRater(user);
                    return newRating;
                });

        rating.setRatingScore(score);
        rating.setComment(comment);
        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public ActivityStatsDto getActivityStats(Long activityId) {
        List<ActivityRating> ratings = ratingRepository.findByActivityId(activityId);

        if (ratings.isEmpty()) {
            return new ActivityStatsDto(0.0, 0L);
        }

        double averageRating = ratings.stream()
                .mapToInt(ActivityRating::getRatingScore)
                .average()
                .orElse(0.0);

        // Format to one decimal place
        averageRating = Math.round(averageRating * 10.0) / 10.0;

        return new ActivityStatsDto(averageRating, (long) ratings.size());
    }

    @Transactional(readOnly = true)
    public List<ActivityRating> getUserRatings(Long userId) {
        return ratingRepository.findByRaterId(userId);
    }

    @Transactional(readOnly = true)
    public List<ActivityRating> getActivityRatings(Long activityId) {
        return ratingRepository.findByActivityId(activityId);
    }
}
