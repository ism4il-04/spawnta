package com.spawnta.controller;

import com.spawnta.dto.ActivityParticipantResponse;
import com.spawnta.dto.ActivityResponse;
import com.spawnta.dto.CreateActivityRequest;
import com.spawnta.dto.JoinActivityRequest;
import com.spawnta.dto.MyActivityResponse;
import com.spawnta.entity.ActivityType;
import com.spawnta.entity.ParticipationMode;
import com.spawnta.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ActivityResponse response = activityService.createActivity(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "10") double radiusKm,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) ParticipationMode participationMode,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) LocalDate scheduledDate) {
        List<ActivityResponse> activities = activityService.findNearby(
            lat,
            lng,
            radiusKm,
            category,
            participationMode,
            activityType,
            scheduledDate
        );
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyActivityResponse>> getMyActivities(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(activityService.getMyActivities(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Map<String, String>> joinActivity(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) JoinActivityRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        activityService.joinActivity(id, email, request);
        return ResponseEntity.ok(Map.of("message", "Successfully joined or requested to join the activity"));
    }

    @PutMapping("/{id}/participants/{participantId}/approve")
    public ResponseEntity<Map<String, String>> approveParticipant(
            @PathVariable Long id,
            @PathVariable Long participantId,
            Authentication authentication) {
        String email = authentication.getName();
        activityService.approveParticipant(id, participantId, email);
        return ResponseEntity.ok(Map.of("message", "Participant approved"));
    }

    @GetMapping("/{id}/participants/pending")
    public ResponseEntity<List<ActivityParticipantResponse>> getPendingParticipants(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(activityService.getPendingParticipants(id, email));
    }
}
