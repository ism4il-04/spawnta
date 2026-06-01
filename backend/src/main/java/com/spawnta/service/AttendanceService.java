package com.spawnta.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spawnta.dto.ActivityParticipationStatusDto;
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
    private final QrCodeService qrCodeService;
    private final ActivityRatingRepository ratingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AttendanceService(ActivityAttendanceRepository attendanceRepository,
                             AttendanceEvidenceRepository evidenceRepository,
                             ActivityRepository activityRepository,
                             UserRepository userRepository,
                             ActivityParticipantRepository participantRepository,
                             AttendanceValidator attendanceValidator,
                             GamificationService gamificationService,
                             OutboxEventRepository outboxEventRepository,
                             QrCodeService qrCodeService,
                             ActivityRatingRepository ratingRepository) {
        this.attendanceRepository = attendanceRepository;
        this.evidenceRepository = evidenceRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.attendanceValidator = attendanceValidator;
        this.gamificationService = gamificationService;
        this.outboxEventRepository = outboxEventRepository;
        this.qrCodeService = qrCodeService;
        this.ratingRepository = ratingRepository;
    }

    @Transactional(readOnly = true)
    public ActivityParticipationStatusDto getMyParticipationStatus(Long activityId, Long userId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        boolean isHost = activity.getHost().getId().equals(userId);

        Optional<ActivityParticipant> participantOpt =
                participantRepository.findByActivityIdAndUserId(activityId, userId);
        boolean joined = participantOpt
                .map(p -> p.getStatus() == ParticipationStatus.APPROVED)
                .orElse(false);
        boolean pendingRequest = participantOpt
                .map(p -> p.getStatus() == ParticipationStatus.PENDING)
                .orElse(false);

        Optional<ActivityAttendance> attendance =
                attendanceRepository.findByActivityIdAndParticipantId(activityId, userId);
        String attendanceStatus = attendance.map(a -> a.getStatus().name()).orElse(null);
        boolean confirmed = attendance.map(a -> a.getStatus() == AttendanceStatus.CONFIRMED).orElse(false);

        // canCheckIn: must be joined, not the host, not already checked in, and within the time window
        boolean alreadyCheckedIn = attendance
                .map(a -> a.getStatus() == AttendanceStatus.PENDING || a.getStatus() == AttendanceStatus.CONFIRMED)
                .orElse(false);
        boolean withinTimeWindow = attendanceValidator.validateTimeWindow(activity, LocalDateTime.now());
        boolean canCheckIn = joined && !isHost && !alreadyCheckedIn && withinTimeWindow;

        boolean canRate = joined && !isHost && confirmed;
        boolean hasRated = ratingRepository.findByActivityIdAndRaterId(activityId, userId).isPresent();

        return new ActivityParticipationStatusDto(isHost, joined, pendingRequest, canCheckIn, canRate, hasRated, attendanceStatus);
    }

    @Transactional
    public AttendanceCheckInDto initiateCheckIn(Long activityId, Long userId, Double latitude, Double longitude) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found with ID: " + activityId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isHost = activity.getHost().getId().equals(userId);
        boolean joined = participantRepository.findByActivityIdAndUserId(activityId, userId)
                .map(p -> p.getStatus() == ParticipationStatus.APPROVED)
                .orElse(false);

        int duration = activity.getDurationMinutes() != null ? activity.getDurationMinutes() : 120;
        LocalDateTime deadline = activity.getScheduledAt().plusMinutes(duration).plusMinutes(30);
        String qrCode = generateActivityQrCode(activityId);

        // Host only needs the QR code to show participants (no self check-in record)
        if (isHost && !joined) {
            return new AttendanceCheckInDto(null, qrCode, activity.getTitle(), deadline);
        }

        if (!joined) {
            throw new IllegalStateException("Join this activity before checking in.");
        }

        ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, userId)
                .orElseGet(() -> {
                    ActivityAttendance newAttendance = new ActivityAttendance(activity, user);
                    return attendanceRepository.save(newAttendance);
                });

        return new AttendanceCheckInDto(attendance.getId(), qrCode, activity.getTitle(), deadline);
    }

    @Transactional
    public ActivityAttendance confirmCheckIn(Long activityId, Long userId, String photoUrl, Double latitude, Double longitude) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (activity.getHost().getId().equals(userId)) {
            throw new IllegalStateException("Hosts cannot check in as participants on their own activity.");
        }

        ActivityAttendance attendance = attendanceRepository.findByActivityIdAndParticipantId(activityId, userId)
                .orElseThrow(() -> new IllegalStateException("You must initiate check-in before confirming."));

        if (attendance.getStatus() == AttendanceStatus.CONFIRMED) {
            throw new IllegalStateException("Your attendance has already been confirmed.");
        }

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
        attendance.setStatus(AttendanceStatus.PENDING); // Awaiting host confirmation
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
        String payload = "spawnta://check-in/" + activityId;
        return qrCodeService.toPngDataUri(payload, 280);
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
