package com.spawnta.controller;

import com.spawnta.dto.ActivityParticipationStatusDto;
import com.spawnta.dto.AttendanceCheckInDto;
import com.spawnta.dto.AttendanceEvidenceDto;
import com.spawnta.dto.CheckInInitiateRequest;
import com.spawnta.dto.HostConfirmRequest;
import com.spawnta.entity.ActivityAttendance;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.spawnta.dto.AttendancePendingResponse;

@RestController
@RequestMapping("/api/activities/{activityId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<ActivityParticipationStatusDto> getMyStatus(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(attendanceService.getMyParticipationStatus(activityId, user.getId()));
    }

    @PostMapping("/check-in/initiate")
    public ResponseEntity<AttendanceCheckInDto> initiateCheckIn(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @RequestBody CheckInInitiateRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(attendanceService.initiateCheckIn(
                activityId,
                user.getId(),
                request.getLatitude(),
                request.getLongitude()
        ));
    }

    @PostMapping("/check-in/confirm")
    public ResponseEntity<Map<String, Object>> confirmCheckIn(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @Valid @RequestBody AttendanceEvidenceDto request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ActivityAttendance attendance = attendanceService.confirmCheckIn(
                activityId,
                user.getId(),
                request.getLatitude(),
                request.getLongitude()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Check-in successful! Attendance confirmed.",
                "attendanceId", attendance.getId(),
                "status", attendance.getStatus().name()
        ));
    }

    @PostMapping("/check-in/qr")
    public ResponseEntity<Map<String, Object>> checkInViaQr(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email,
            @RequestBody Map<String, String> request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = request.get("token");
        if (token == null) {
            throw new IllegalArgumentException("QR Code Token is required");
        }

        ActivityAttendance attendance = attendanceService.checkInViaQr(
                activityId,
                user.getId(),
                token
        );

        return ResponseEntity.ok(Map.of(
                "message", "Check-in successful via QR! Attendance confirmed.",
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
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        attendanceService.hostConfirmAttendance(
                activityId,
                host.getId(),
                request.getParticipantIds()
        );

        return ResponseEntity.ok(Map.of("message", "Attendance confirmed for selected participants."));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<AttendancePendingResponse>> getPendingAttendances(
            @PathVariable Long activityId,
            @AuthenticationPrincipal String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(attendanceService.getPendingAttendances(activityId, user.getId()));
    }
}
