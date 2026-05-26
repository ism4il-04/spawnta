package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.dto.AttendanceCheckInDto;
import com.spawnta.entity.*;
import com.spawnta.repository.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    private final ActivityAttendanceRepository attendanceRepository;
    private final AttendanceEvidenceRepository evidenceRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository participantRepository;
    private final AttendanceValidator attendanceValidator;
    private final GamificationService gamificationService;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AttendanceService(ActivityAttendanceRepository attendanceRepository,
                             AttendanceEvidenceRepository evidenceRepository,
                             ActivityRepository activityRepository,
                             UserRepository userRepository,
                             ActivityParticipantRepository participantRepository,
                             AttendanceValidator attendanceValidator,
                             GamificationService gamificationService,
                             OutboxEventRepository outboxEventRepository) {
        this.attendanceRepository = attendanceRepository;
        this.evidenceRepository = evidenceRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.attendanceValidator = attendanceValidator;
        this.gamificationService = gamificationService;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public AttendanceCheckInDto initiateCheckIn(Long activityId, Long userId, Double latitude, Double longitude) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Check if user is a participant
        boolean isParticipant = participantRepository.existsByActivityIdAndUserId(activityId, userId);
        if (!isParticipant && !activity.getHost().getId().equals(userId)) {
            throw new IllegalStateException("User is not a registered participant in this activity");
        }

        // Get or Create Attendance
        ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, userId)
                .orElseGet(() -> {
                    ActivityAttendance newAttendance = new ActivityAttendance(activity, user);
                    return attendanceRepository.save(newAttendance);
                });

        int duration = activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 120;
        LocalDateTime deadline = activity.getScheduledAt().plusMinutes(duration).plusMinutes(30);

        String qrCodeUrl = generateActivityQrCode(activityId);

        return new AttendanceCheckInDto(attendance.getId(), qrCodeUrl, activity.getTitle(), deadline);
    }

    @Transactional
    public ActivityAttendance confirmCheckIn(Long attendanceId, String photoUrl, Double latitude, Double longitude) {
        ActivityAttendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance not found with ID: " + attendanceId));

        Activity activity = attendance.getActivity();
        LocalDateTime now = LocalDateTime.now();

        // 1. Time Window Validation
        if (!attendanceValidator.validateTimeWindow(activity, now)) {
            throw new IllegalStateException("Check-in is only allowed during the active activity time window");
        }

        // 2. Geolocation Validation
        Point checkinLoc = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Point activityLoc = activity.getActivityType() == ActivityType.MEETUP ? activity.getLocation() : activity.getStartLocation();
        
        if (!attendanceValidator.validateGeolocation(activityLoc, checkinLoc)) {
            throw new IllegalStateException("You are too far from the activity location to check in");
        }

        // 3. Photo Evidence Validation
        if (!attendanceValidator.validatePhotoEvidence(photoUrl)) {
            throw new IllegalArgumentException("Invalid photo evidence provided");
        }

        // Save Evidence
        AttendanceEvidence evidence = new AttendanceEvidence(attendance, photoUrl, checkinLoc);
        evidenceRepository.save(evidence);

        // Update Check-in Status
        attendance.setCheckInTime(now);
        attendance.setStatus(AttendanceStatus.PENDING); // Awaiting host confirmation or auto-confirmed on host action
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void hostConfirmAttendance(Long activityId, Long hostId, List<Long> participantIds) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!activity.getHost().getId().equals(hostId)) {
            throw new IllegalStateException("Only the host can confirm attendance for this activity");
        }

        LocalDateTime now = LocalDateTime.now();

        for (Long participantId : participantIds) {
            ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, participantId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(participantId)
                                .orElseThrow(() -> new IllegalArgumentException("Participant user not found"));
                        ActivityAttendance newAttendance = new ActivityAttendance(activity, user);
                        return attendanceRepository.save(newAttendance);
                    });

            attendance.setConfirmedByHost(true);
            attendance.setConfirmedAt(now);
            attendance.setStatus(AttendanceStatus.CONFIRMED);
            attendanceRepository.save(attendance);

            // Award XP: +100 XP for attending an activity
            gamificationService.awardXp(participantId, 100, "Participated in: " + activity.getTitle());

            // Write transactional outbox event for Kafka
            createAttendanceConfirmedOutboxEvent(attendance);
        }
    }

    public String generateActivityQrCode(Long activityId) {
        // Generates the deep check-in payload signature
        return "spawnta://check-in/" + activityId;
    }

    private void createAttendanceConfirmedOutboxEvent(ActivityAttendance attendance) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("eventId", UUID.randomUUID().toString());
            payload.put("attendanceId", attendance.getId());
            payload.put("activityId", attendance.getActivity().getId());
            payload.put("participantId", attendance.getParticipant().getId());
            payload.put("participantEmail", attendance.getParticipant().getEmail());
            payload.put("confirmedAt", attendance.getConfirmedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            payload.put("action", "ATTENDANCE_CONFIRMED");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent("attendance.confirmed", jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Failed to serialize attendance confirmed event to outbox", e);
        }
    }
}
