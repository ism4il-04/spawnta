package com.spawnta.controller;

import com.spawnta.dto.GenerateRecommendationsRequest;
import com.spawnta.entity.Activity;
import com.spawnta.entity.ActivityRecommendation;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService, UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Map<String, Object>>> generateRecommendations(
            @AuthenticationPrincipal String email,
            @RequestBody GenerateRecommendationsRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ActivityRecommendation> recs = recommendationService.generateRecommendations(
                user.getId(),
                request.getLatitude(),
                request.getLongitude()
        );

        List<Map<String, Object>> response = recs.stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId());
                    map.put("score", r.getScore());
                    map.put("reason", r.getReason() != null ? r.getReason() : "");
                    map.put("activityId", r.getActivity().getId());
                    map.put("title", r.getActivity().getTitle());
                    map.put("category", r.getActivity().getCategory() != null ? r.getActivity().getCategory() : "");
                    map.put("scheduledAt", r.getActivity().getScheduledAt().toString());
                    map.put("hostName", r.getActivity().getHost().getFirstName() + " " + r.getActivity().getHost().getLastName());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Map<String, Object>>> getPersonalizedFeed(@AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ActivityRecommendation> feed = recommendationService.getPersonalizedFeedWithRecs(user.getId());

        List<Map<String, Object>> response = feed.stream()
                .map(r -> {
                    Activity a = r.getActivity();
                    Map<String, Object> map = new HashMap<>();
                    map.put("recommendationId", r.getId());
                    map.put("activityId", a.getId());
                    map.put("title", a.getTitle());
                    map.put("description", a.getDescription() != null ? a.getDescription() : "");
                    map.put("category", a.getCategory() != null ? a.getCategory() : "");
                    map.put("scheduledAt", a.getScheduledAt().toString());
                    map.put("hostName", a.getHost().getFirstName() + " " + a.getHost().getLastName());
                    map.put("hostAvatarUrl", a.getHost().getAvatarUrl() != null ? a.getHost().getAvatarUrl() : "");
                    map.put("reason", r.getReason());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{recommendationId}/click")
    public ResponseEntity<Map<String, String>> trackClick(@PathVariable Long recommendationId) {
        recommendationService.trackClick(recommendationId);
        return ResponseEntity.ok(Map.of("message", "Click tracked successfully."));
    }
}
