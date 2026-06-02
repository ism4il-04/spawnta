package com.spawnta.admin.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spawnta.admin.dto.AdminActivitiesResponseDTO;
import com.spawnta.admin.dto.AdminActivityDTO;
import com.spawnta.admin.dto.DeleteActivityRequest;
import com.spawnta.admin.entity.AdminAuditLog;
import com.spawnta.admin.repository.AdminAuditLogRepository;
import com.spawnta.entity.Activity;
import com.spawnta.entity.User;
import com.spawnta.moderation.repository.ActivityReportRepository;
import com.spawnta.repository.ActivityRepository;
import com.spawnta.repository.UserRepository;

@Service
public class AdminActivitiesService {

    private final ActivityRepository activityRepository;
    private final ActivityReportRepository activityReportRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminActivitiesService(
            ActivityRepository activityRepository,
            ActivityReportRepository activityReportRepository,
            UserRepository userRepository,
            AdminAuditLogRepository auditLogRepository) {
        this.activityRepository = activityRepository;
        this.activityReportRepository = activityReportRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminActivitiesResponseDTO listActivities(String search, String status, String category) {
        LocalDateTime now = LocalDateTime.now();
        String normalizedSearch = normalize(search);
        String normalizedStatus = normalize(status);
        String normalizedCategory = normalize(category);

        List<Activity> allActivities = activityRepository.findAll();
        List<AdminActivityDTO> filteredActivities = allActivities.stream()
                .filter(activity -> matchesSearch(activity, normalizedSearch))
                .filter(activity -> matchesStatus(activity, normalizedStatus, now))
                .filter(activity -> matchesCategory(activity, normalizedCategory))
                .sorted(Comparator.comparing(Activity::getScheduledAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toDto)
                .toList();

        return new AdminActivitiesResponseDTO(
                filteredActivities,
                allActivities.size(),
                allActivities.stream().filter(activity -> isUpcoming(activity, now)).count(),
                allActivities.stream().filter(activity -> !isUpcoming(activity, now)).count()
        );
    }

    @Transactional
    public void deleteActivity(Long activityId, DeleteActivityRequest request, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        String reason = request == null || request.reason() == null || request.reason().isBlank()
                ? "Deleted by admin"
                : request.reason().trim();
        activityReportRepository.deleteByActivityId(activityId);
        activityRepository.delete(activity);
        auditLogRepository.save(AdminAuditLog.builder()
                .admin(admin)
                .action("DELETE_ACTIVITY")
                .targetType("ACTIVITY")
                .targetId(activityId)
                .details(reason)
                .build());
    }

    private AdminActivityDTO toDto(Activity activity) {
        User host = activity.getHost();
        return new AdminActivityDTO(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getActivityType(),
                activity.getParticipationMode(),
                activity.getCategory(),
                activity.getMaxParticipants(),
                activity.getParticipants() == null ? 0 : activity.getParticipants().size(),
                activity.getScheduledAt(),
                activity.getCreatedAt(),
                host.getId(),
                host.getEmail(),
                host.getFirstName() + " " + host.getLastName()
        );
    }

    private boolean matchesSearch(Activity activity, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        User host = activity.getHost();
        String haystack = (activity.getTitle() + " " + activity.getDescription() + " " + activity.getCategory()
                + " " + host.getEmail() + " " + host.getFirstName() + " " + host.getLastName())
                .toLowerCase(Locale.ROOT);
        return haystack.contains(search);
    }

    private boolean matchesStatus(Activity activity, String status, LocalDateTime now) {
        if (status == null || status.isBlank() || "all".equals(status)) {
            return true;
        }
        return switch (status) {
            case "upcoming" -> isUpcoming(activity, now);
            case "past" -> !isUpcoming(activity, now);
            default -> true;
        };
    }

    private boolean matchesCategory(Activity activity, String category) {
        return category == null
                || category.isBlank()
                || "all".equals(category)
                || category.equals(normalize(activity.getCategory()));
    }

    private boolean isUpcoming(Activity activity, LocalDateTime now) {
        return activity.getScheduledAt() != null && activity.getScheduledAt().isAfter(now);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
