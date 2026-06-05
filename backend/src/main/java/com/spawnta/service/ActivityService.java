package com.spawnta.service;

import com.spawnta.dto.ActivityParticipantResponse;
import com.spawnta.dto.ActivityParticipationStatusDto;
import com.spawnta.dto.ActivityResponse;
import com.spawnta.dto.CreateActivityRequest;
import com.spawnta.dto.JoinActivityRequest;
import com.spawnta.dto.MyActivityResponse;
import com.spawnta.entity.*;
import com.spawnta.repository.ActivityParticipantRepository;
import com.spawnta.repository.ActivityRepository;
import com.spawnta.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;
    private final AttendanceService attendanceService;
    private final BadgeService badgeService;
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public ActivityService(ActivityRepository activityRepository,
                           ActivityParticipantRepository participantRepository,
                           UserRepository userRepository,
                           ChatService chatService,
                           AttendanceService attendanceService,
                           BadgeService badgeService) {
        this.activityRepository = activityRepository;
        this.participantRepository = participantRepository;
        this.userRepository = userRepository;
        this.chatService = chatService;
        this.attendanceService = attendanceService;
        this.badgeService = badgeService;
    }

    @Transactional
    public ActivityResponse createActivity(CreateActivityRequest request, String userEmail) {
        User host = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check limits for non-premium users
        if (!host.isPremium()) {
            LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
            long recentCount = activityRepository.countByHostIdAndCreatedAtAfter(host.getId(), oneWeekAgo);
            if (recentCount >= 10) { // TODO: revert to 2 in production
                throw new IllegalStateException("Free users can only create 2 activities per week. Upgrade to Premium for unlimited creations.");
            }
        }

        Activity activity = new Activity();
        activity.setTitle(request.title());
        activity.setDescription(request.description());
        activity.setActivityType(request.activityType());
        activity.setParticipationMode(request.participationMode() != null ? request.participationMode() : ParticipationMode.DIRECT);
        activity.setMaxParticipants(request.maxParticipants());
        activity.setScheduledAt(request.scheduledAt());
        activity.setDurationMinutes(request.durationMinutes());
        activity.setCategory(request.category());
        activity.setAddress(request.address());
        activity.setHost(host);

        // Map spatial coordinates to JTS Points (SRID 4326)
        if (request.activityType() == ActivityType.MEETUP) {
            if (request.latitude() == null || request.longitude() == null) {
                throw new IllegalArgumentException("Meetups require latitude and longitude");
            }
            activity.setLocation(createPoint(request.longitude(), request.latitude()));
        } else if (request.activityType() == ActivityType.TRIP) {
            if (request.startLatitude() == null || request.startLongitude() == null ||
                request.destLatitude() == null || request.destLongitude() == null) {
                throw new IllegalArgumentException("Trips require start and destination coordinates");
            }
            activity.setStartLocation(createPoint(request.startLongitude(), request.startLatitude()));
            activity.setDestination(createPoint(request.destLongitude(), request.destLatitude()));
        }

        activity = activityRepository.save(activity);

        // Host is automatically joined as approved
        ActivityParticipant hostParticipation = new ActivityParticipant(activity, host, ParticipationStatus.APPROVED, null);
        participantRepository.save(hostParticipation);

        // Auto-create Group Chat
        chatService.createGroupChat(activity);

        // Trigger badge check for host (Activity Master, Community Leader)
        badgeService.checkBadgeCriteria(host.getId());

        return mapToResponse(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> findNearby(
        double lat,
        double lng,
        double radiusKm,
        String category,
        ParticipationMode participationMode,
        ActivityType activityType,
        LocalDate scheduledDate
    ) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Invalid coordinates. Latitude must be between -90 and 90, longitude between -180 and 180.");
        }

        double radiusMeters = radiusKm * 1000;
        LocalDateTime scheduledFrom = scheduledDate != null ? scheduledDate.atStartOfDay() : null;
        LocalDateTime scheduledTo = scheduledDate != null ? scheduledDate.atTime(LocalTime.MAX) : null;
        String categoryFilter = normalize(category);
        String participationModeFilter = participationMode != null ? participationMode.name() : null;
        String activityTypeFilter = activityType != null ? activityType.name() : null;
        
        List<Activity> meetups = activityRepository.findNearbyMeetups(
            lat,
            lng,
            radiusMeters,
            categoryFilter,
            participationModeFilter,
            activityTypeFilter,
            scheduledFrom,
            scheduledTo
        );
        List<Activity> trips = activityRepository.findNearbyTrips(
            lat,
            lng,
            radiusMeters,
            categoryFilter,
            participationModeFilter,
            activityTypeFilter,
            scheduledFrom,
            scheduledTo
        );
        
        // Combine and sort by scheduled time
        meetups.addAll(trips);
        return meetups.stream()
            .distinct() // in case some activities matched both somehow
            .sorted((a1, a2) -> a1.getScheduledAt().compareTo(a2.getScheduledAt()))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ActivityResponse getById(Long id) {
        Activity activity = activityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
        return mapToResponse(activity);
    }

    @Transactional
    public void joinActivity(Long activityId, String userEmail, JoinActivityRequest request) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
            
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (participantRepository.existsByActivityIdAndUserId(activityId, user.getId())) {
            throw new IllegalStateException("You have already joined or requested to join this activity");
        }

        if (activity.getHost().getId().equals(user.getId())) {
            throw new IllegalStateException("The host is already part of the activity");
        }

        // Check capacity
        if (activity.getMaxParticipants() != null) {
            long approvedCount = participantRepository.countByActivityIdAndStatus(activityId, ParticipationStatus.APPROVED);
            if (approvedCount >= activity.getMaxParticipants()) {
                throw new IllegalStateException("Activity is full");
            }
        }

        ParticipationStatus status = activity.getParticipationMode() == ParticipationMode.DIRECT
            ? ParticipationStatus.APPROVED
            : ParticipationStatus.PENDING;

        String intro = request != null ? request.introMessage() : null;
        if (activity.getParticipationMode() == ParticipationMode.APPROVAL && (intro == null || intro.isBlank())) {
            throw new IllegalArgumentException("An introduction message is required for approval-based activities");
        }
        ActivityParticipant participant = new ActivityParticipant(activity, user, status, intro);
        participantRepository.save(participant);

        if (status == ParticipationStatus.APPROVED) {
            chatService.addParticipantToGroupChat(activityId, user.getId());
        }
    }

    @Transactional
    public void approveParticipant(Long activityId, Long participantId, String hostEmail) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found"));
            
        if (!activity.getHost().getEmail().equals(hostEmail)) {
            throw new IllegalStateException("Only the host can approve participants");
        }

        ActivityParticipant participant = participantRepository.findById(participantId)
            .orElseThrow(() -> new IllegalArgumentException("Participant request not found"));

        if (!participant.getActivity().getId().equals(activityId)) {
            throw new IllegalArgumentException("Participant does not belong to this activity");
        }

        if (activity.getMaxParticipants() != null) {
            long approvedCount = participantRepository.countByActivityIdAndStatus(activityId, ParticipationStatus.APPROVED);
            if (approvedCount >= activity.getMaxParticipants()) {
                throw new IllegalStateException("Activity is full");
            }
        }

        participant.setStatus(ParticipationStatus.APPROVED);
        participantRepository.save(participant);

        chatService.addParticipantToGroupChat(activityId, participant.getUser().getId());
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantResponse> getPendingParticipants(Long activityId, String hostEmail) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!activity.getHost().getEmail().equals(hostEmail)) {
            throw new IllegalStateException("Only the host can view pending requests");
        }

        return participantRepository.findAllByActivityIdAndStatusOrderByJoinedAtAsc(activityId, ParticipationStatus.PENDING)
            .stream()
            .map(participant -> new ActivityParticipantResponse(
                participant.getId(),
                participant.getUser().getId(),
                participant.getUser().getFirstName(),
                participant.getUser().getLastName(),
                participant.getUser().getEmail(),
                participant.getStatus(),
                participant.getIntroMessage(),
                participant.getJoinedAt()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MyActivityResponse> getMyActivities(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<Long, MyActivityResponse> map = new LinkedHashMap<>();

        // Hosted activities
        for (Activity act : activityRepository.findByHostId(user.getId())) {
            ActivityResponse resp = mapToResponse(act);
            ActivityParticipationStatusDto status = attendanceService.getMyParticipationStatus(act.getId(), user.getId());
            map.put(act.getId(), new MyActivityResponse(resp, status));
        }

        // Joined / requested activities
        for (ActivityParticipant part : participantRepository.findByUserId(user.getId())) {
            Activity act = part.getActivity();
            if (!map.containsKey(act.getId())) {
                ActivityResponse resp = mapToResponse(act);
                ActivityParticipationStatusDto status = attendanceService.getMyParticipationStatus(act.getId(), user.getId());
                map.put(act.getId(), new MyActivityResponse(resp, status));
            }
        }

        return new ArrayList<>(map.values());
    }

    // ─── Helpers ─────────────────────────────────────────

    private Point createPoint(double longitude, double latitude) {
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(4326); // WGS84
        return point;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private ActivityResponse mapToResponse(Activity a) {
        int approvedParticipants = (int) a.getParticipants().stream()
            .filter(p -> p.getStatus() == ParticipationStatus.APPROVED)
            .count();

        Double lat = a.getLocation() != null ? a.getLocation().getY() : null;
        Double lng = a.getLocation() != null ? a.getLocation().getX() : null;
        
        Double startLat = a.getStartLocation() != null ? a.getStartLocation().getY() : null;
        Double startLng = a.getStartLocation() != null ? a.getStartLocation().getX() : null;
        
        Double destLat = a.getDestination() != null ? a.getDestination().getY() : null;
        Double destLng = a.getDestination() != null ? a.getDestination().getX() : null;

        return new ActivityResponse(
            a.getId(),
            a.getTitle(),
            a.getDescription(),
            a.getActivityType(),
            a.getParticipationMode(),
            a.getMaxParticipants(),
            a.getScheduledAt(),
            a.getDurationMinutes(),
            a.getCategory(),
            lat, lng,
            startLat, startLng, destLat, destLng,
            a.getAddress(),
            a.getHost().getId(),
            a.getHost().getFirstName(),
            a.getHost().getLastName(),
            a.getHost().getEmail(),
            approvedParticipants,
            a.getCreatedAt()
        );
    }
}
