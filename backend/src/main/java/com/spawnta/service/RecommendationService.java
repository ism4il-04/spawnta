package com.spawnta.service;

import com.spawnta.dto.ActivityStatsDto;
import com.spawnta.entity.*;
import com.spawnta.repository.ActivityRecommendationRepository;
import com.spawnta.repository.ActivityRepository;
import com.spawnta.repository.UserRepository;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final ActivityRecommendationRepository recommendationRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityRatingService ratingService;

    public RecommendationService(ActivityRecommendationRepository recommendationRepository,
                                 ActivityRepository activityRepository,
                                 UserRepository userRepository,
                                 ActivityRatingService ratingService) {
        this.recommendationRepository = recommendationRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.ratingService = ratingService;
    }

    @Transactional
    public List<ActivityRecommendation> generateRecommendations(Long userId, Double userLat, Double userLng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        log.info("Generating activity recommendations for user: {}", user.getEmail());

        List<Activity> futureActivities = activityRepository.findAllByScheduledAtAfter(LocalDateTime.now());
        List<ActivityRecommendation> recommendations = new ArrayList<>();

        for (Activity activity : futureActivities) {
            // Exclude activities hosted by the user themselves
            if (activity.getHost().getId().equals(userId)) {
                continue;
            }

            double score = calculateScore(user, activity, userLat, userLng);

            ActivityRecommendation rec = recommendationRepository.findByUserIdAndActivityId(userId, activity.getId())
                    .orElseGet(() -> {
                        ActivityRecommendation newRec = new ActivityRecommendation();
                        newRec.setUser(user);
                        newRec.setActivity(activity);
                        return newRec;
                    });

            rec.setScore(BigDecimal.valueOf(score));
            rec.setReason(determineRecommendationReason(user, activity, score));
            rec.setClicked(false);
            rec.setClickedAt(null);
            recommendations.add(recommendationRepository.save(rec));
        }

        return recommendations;
    }

    @Transactional
    public void trackClick(Long recommendationId) {
        ActivityRecommendation rec = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found with ID: " + recommendationId));

        rec.setClicked(true);
        rec.setClickedAt(LocalDateTime.now());
        recommendationRepository.save(rec);
    }

    @Transactional(readOnly = true)
    public List<ActivityRecommendation> getPersonalizedFeedWithRecs(Long userId) {
        return recommendationRepository.findByUserIdAndClickedFalseOrderByScoreDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Activity> getPersonalizedFeed(Long userId) {
        List<ActivityRecommendation> recs = recommendationRepository.findByUserIdAndClickedFalseOrderByScoreDesc(userId);
        return recs.stream()
                .map(ActivityRecommendation::getActivity)
                .collect(Collectors.toList());
    }

    /**
     * Scoring formula: Max 100 points
     * 1. Distance Matching (Max 50 pts)
     * 2. Category interest matching (Max 30 pts)
     * 3. Activity popularity/rating (Max 20 pts)
     */
    public double calculateScore(User user, Activity activity, Double userLat, Double userLng) {
        double totalScore = 0.0;

        // 1. Distance Matching (up to 50 points)
        if (userLat != null && userLng != null) {
            Point actLoc = activity.getActivityType() == ActivityType.MEETUP ? activity.getLocation() : activity.getStartLocation();
            if (actLoc != null) {
                double distanceKm = calculateDistanceInKm(userLat, userLng, actLoc.getY(), actLoc.getX());
                if (distanceKm <= 5.0) {
                    totalScore += 50.0;
                } else if (distanceKm <= 15.0) {
                    totalScore += 35.0;
                } else if (distanceKm <= 50.0) {
                    totalScore += 20.0;
                }
            }
        }

        // 2. Category matching (up to 30 points)
        String actCat = activity.getCategory();
        if (actCat != null && !actCat.isBlank()) {
            boolean matchesInterest = user.getInterests().stream()
                    .anyMatch(interest -> interest.name().equalsIgnoreCase(actCat));
            if (matchesInterest) {
                totalScore += 30.0;
            }
        }

        // 3. Popularity & Ratings (up to 20 points)
        ActivityStatsDto stats = ratingService.getActivityStats(activity.getId());
        double ratingPoints = stats.getAverageRating() * 2; // e.g. 4.5 rating -> 9 points (max 10)
        totalScore += Math.min(ratingPoints, 10.0);

        int participantsCount = activity.getParticipants() != null ? activity.getParticipants().size() : 0;
        double participantPoints = participantsCount * 1.0; // 1 point per participant (max 10)
        totalScore += Math.min(participantPoints, 10.0);

        return Math.round(totalScore * 100.0) / 100.0; // round to 2 decimals
    }

    private String determineRecommendationReason(User user, Activity activity, double score) {
        String actCat = activity.getCategory();
        if (actCat != null && !actCat.isBlank()) {
            boolean matchesInterest = user.getInterests().stream()
                    .anyMatch(interest -> interest.name().equalsIgnoreCase(actCat));
            if (matchesInterest) {
                return "Based on your interest in " + actCat;
            }
        }

        if (score >= 70.0) {
            return "Popular near you";
        } else if (score >= 40.0) {
            return "Recommended activity for you";
        }

        return "Recommended for the community";
    }

    private double calculateDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c; // Earth radius in km
    }
}
