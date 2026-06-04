package com.spawnta.service;

import com.spawnta.dto.ActivityRatingViewDto;
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
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (activity.getHost().getId().equals(userId)) {
            throw new IllegalStateException("The host cannot rate their own activity.");
        }

        ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Only participants with confirmed attendance can leave a review."));

        if (attendance.getStatus() != AttendanceStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Your attendance must be confirmed by the host before you can rate this activity.");
        }

        log.info("User {} rating activity {} with score {}", user.getEmail(), activityId, score);

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
        if (!activityRepository.existsById(activityId)) {
            throw new IllegalArgumentException("Activity not found");
        }

        List<ActivityRating> ratings = ratingRepository.findByActivityId(activityId);

        if (ratings.isEmpty()) {
            return new ActivityStatsDto(0.0, 0L);
        }

        double averageRating = ratings.stream()
                .mapToInt(ActivityRating::getRatingScore)
                .average()
                .orElse(0.0);

        averageRating = Math.round(averageRating * 10.0) / 10.0;
        return new ActivityStatsDto(averageRating, (long) ratings.size());
    }

    @Transactional(readOnly = true)
    public List<ActivityRatingViewDto> getActivityRatingViews(Long activityId) {
        if (!activityRepository.existsById(activityId)) {
            throw new IllegalArgumentException("Activity not found");
        }

        return ratingRepository.findByActivityIdWithRater(activityId).stream()
                .map(r -> new ActivityRatingViewDto(
                        r.getId(),
                        r.getRatingScore(),
                        r.getComment() != null ? r.getComment() : "",
                        r.getRater().getFirstName() + " " + r.getRater().getLastName(),
                        r.getRater().getAvatarUrl() != null ? r.getRater().getAvatarUrl() : "",
                        r.getCreatedAt().toString()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasUserRated(Long activityId, Long userId) {
        return ratingRepository.findByActivityIdAndRaterId(activityId, userId).isPresent();
    }
}
