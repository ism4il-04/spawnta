package com.spawnta.controller;

import com.spawnta.dto.ActivityRatingViewDto;
import com.spawnta.dto.ActivityStatsDto;
import com.spawnta.dto.RateActivityRequest;
import com.spawnta.entity.ActivityRating;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.ActivityRatingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return ResponseEntity.ok(ratingService.getActivityStats(activityId));
    }

    @GetMapping
    public ResponseEntity<List<ActivityRatingViewDto>> getActivityRatings(@PathVariable Long activityId) {
        return ResponseEntity.ok(ratingService.getActivityRatingViews(activityId));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
