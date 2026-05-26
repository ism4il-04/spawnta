package com.spawnta.controller;

import com.spawnta.dto.ActivityStatsDto;
import com.spawnta.dto.RateActivityRequest;
import com.spawnta.entity.ActivityRating;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.ActivityRatingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities/{activityId}/ratings")
public class ActivityRatingController {

    private final ActivityRatingService ratingService;
    private final UserRepository userRepository;

    public ActivityRatingController(ActivityRatingService ratingService, UserRepository userRepository) {
        this.ratingService = ratingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> rateActivity(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @Valid @RequestBody RateActivityRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ActivityRating rating = ratingService.rateActivity(
                activityId,
                user.getId(),
                request.getRatingScore(),
                request.getComment()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Activity rated successfully.",
                "ratingId", rating.getId(),
                "score", rating.getRatingScore()
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<ActivityStatsDto> getActivityStats(@PathVariable Long activityId) {
        ActivityStatsDto stats = ratingService.getActivityStats(activityId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getActivityRatings(@PathVariable Long activityId) {
        List<ActivityRating> ratings = ratingService.getActivityRatings(activityId);

        List<Map<String, Object>> response = ratings.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("ratingScore", r.getRatingScore());
                    map.put("comment", r.getComment() != null ? r.getComment() : "");
                    map.put("raterName", r.getRater().getFirstName() + " " + r.getRater().getLastName());
                    map.put("raterAvatarUrl", r.getRater().getAvatarUrl() != null ? r.getRater().getAvatarUrl() : "");
                    map.put("createdAt", r.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
