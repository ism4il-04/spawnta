package com.spawnta.controller;

import com.spawnta.dto.AttendanceCheckInDto;
import com.spawnta.dto.AttendanceEvidenceDto;
import com.spawnta.dto.CheckInInitiateRequest;
import com.spawnta.dto.HostConfirmRequest;
import com.spawnta.entity.ActivityAttendance;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activities/{activityId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @PostMapping("/check-in/initiate")
    public ResponseEntity<AttendanceCheckInDto> initiateCheckIn(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @RequestBody CheckInInitiateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        AttendanceCheckInDto checkInDto = attendanceService.initiateCheckIn(
                activityId,
                user.getId(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(checkInDto);
    }

    @PostMapping("/check-in/confirm")
    public ResponseEntity<Map<String, Object>> confirmCheckIn(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @Valid @RequestBody AttendanceEvidenceDto request) {

        // Note: activityId is in path for consistency, but we query attendanceId inside the DTO if needed.
        // Wait, the AttendanceService.confirmCheckIn takes attendanceId directly. Let's find the attendanceId
        // by looking up the user ID and activity ID first!
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ActivityAttendance attendance = attendanceService.confirmCheckIn(
                attendanceService.initiateCheckIn(activityId, user.getId(), request.getLatitude(), request.getLongitude()).getAttendanceId(),
                request.getPhotoUrl(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Check-in completed successfully. Awaiting host confirmation.",
                "attendanceId", attendance.getId(),
                "status", attendance.getStatus().name()
        ));
    }

    @PostMapping("/host-confirm")
    public ResponseEntity<Map<String, String>> hostConfirmAttendance(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @RequestBody HostConfirmRequest request) {

        User host = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Host user not found"));

        attendanceService.hostConfirmAttendance(
                activityId,
                host.getId(),
                request.getParticipantIds()
        );

        return ResponseEntity.ok(Map.of("message", "Attendance confirmed successfully for participants."));
    }
}
