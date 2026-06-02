package com.spawnta.admin.service;

import com.spawnta.admin.dto.AdminModerationDTO;
import com.spawnta.admin.dto.ResolveReportRequest;
import com.spawnta.admin.entity.AdminAuditLog;
import com.spawnta.admin.repository.AdminAuditLogRepository;
import com.spawnta.entity.Activity;
import com.spawnta.entity.User;
import com.spawnta.moderation.entity.ActivityReport;
import com.spawnta.moderation.entity.ReportStatus;
import com.spawnta.moderation.entity.UserReport;
import com.spawnta.moderation.repository.ActivityReportRepository;
import com.spawnta.moderation.repository.UserReportRepository;
import com.spawnta.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminModerationService {

    private final UserReportRepository userReportRepository;
    private final ActivityReportRepository activityReportRepository;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminModerationService(
            UserReportRepository userReportRepository,
            ActivityReportRepository activityReportRepository,
            UserRepository userRepository,
            AdminAuditLogRepository auditLogRepository) {
        this.userReportRepository = userReportRepository;
        this.activityReportRepository = activityReportRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminModerationDTO getReports(String status) {
        ReportStatus selectedStatus = parseStatus(status);
        List<UserReport> userReports = selectedStatus == null ? userReportRepository.findAll() : userReportRepository.findByStatus(selectedStatus);
        List<ActivityReport> activityReports = selectedStatus == null ? activityReportRepository.findAll() : activityReportRepository.findByStatus(selectedStatus);

        return new AdminModerationDTO(
            userReportRepository.countByStatus(ReportStatus.OPEN),
            userReportRepository.countByStatus(ReportStatus.INVESTIGATING),
            userReportRepository.countByStatus(ReportStatus.RESOLVED),
            userReportRepository.countByStatus(ReportStatus.DISMISSED),
            activityReportRepository.countByStatus(ReportStatus.OPEN),
            activityReportRepository.countByStatus(ReportStatus.INVESTIGATING),
            activityReportRepository.countByStatus(ReportStatus.RESOLVED),
            activityReportRepository.countByStatus(ReportStatus.DISMISSED),
            userReports.stream()
                .sorted(Comparator.comparing(UserReport::getCreatedAt).reversed())
                .map(this::mapUserReport)
                .toList(),
            activityReports.stream()
                .sorted(Comparator.comparing(ActivityReport::getCreatedAt).reversed())
                .map(this::mapActivityReport)
                .toList()
        );
    }

    @Transactional
    public AdminModerationDTO.UserReportDTO updateUserReport(Long id, ReportStatus status, ResolveReportRequest request, Authentication authentication) {
        UserReport report = userReportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Signalement utilisateur introuvable."));
        User admin = getAdmin(authentication);
        report.setStatus(status);
        report.setResolutionNotes(notes(request));
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());
        UserReport saved = userReportRepository.save(report);
        log(admin, "USER_REPORT_" + status.name(), "USER_REPORT", saved.getId(), notes(request));
        return mapUserReport(saved);
    }

    @Transactional
    public AdminModerationDTO.ActivityReportDTO updateActivityReport(Long id, ReportStatus status, ResolveReportRequest request, Authentication authentication) {
        ActivityReport report = activityReportRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Signalement activite introuvable."));
        User admin = getAdmin(authentication);
        report.setStatus(status);
        report.setResolutionNotes(notes(request));
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());
        ActivityReport saved = activityReportRepository.save(report);
        log(admin, "ACTIVITY_REPORT_" + status.name(), "ACTIVITY_REPORT", saved.getId(), notes(request));
        return mapActivityReport(saved);
    }

    private AdminModerationDTO.UserReportDTO mapUserReport(UserReport report) {
        User reportedUser = report.getReportedUser();
        User resolvedBy = report.getResolvedBy();
        return new AdminModerationDTO.UserReportDTO(
            report.getId(),
            report.getStatus().name(),
            report.getReason(),
            report.getDescription(),
            report.getReportedBy().getEmail(),
            reportedUser.getId(),
            reportedUser.getEmail(),
            reportedUser.getFirstName() + " " + reportedUser.getLastName(),
            report.getResolutionNotes(),
            resolvedBy == null ? null : resolvedBy.getEmail(),
            report.getCreatedAt(),
            report.getResolvedAt()
        );
    }

    private AdminModerationDTO.ActivityReportDTO mapActivityReport(ActivityReport report) {
        Activity activity = report.getActivity();
        User resolvedBy = report.getResolvedBy();
        return new AdminModerationDTO.ActivityReportDTO(
            report.getId(),
            report.getStatus().name(),
            report.getReason(),
            report.getDescription(),
            report.getReportedBy().getEmail(),
            activity.getId(),
            activity.getTitle(),
            activity.getHost().getEmail(),
            report.getResolutionNotes(),
            resolvedBy == null ? null : resolvedBy.getEmail(),
            report.getCreatedAt(),
            report.getResolvedAt()
        );
    }

    private ReportStatus parseStatus(String status) {
        if (status == null || status.isBlank() || "all".equalsIgnoreCase(status)) {
            return null;
        }
        return ReportStatus.valueOf(status.toUpperCase());
    }

    private User getAdmin(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("Admin introuvable."));
    }

    private String notes(ResolveReportRequest request) {
        return request == null || request.notes() == null || request.notes().isBlank()
            ? "Decision admin"
            : request.notes().trim();
    }

    private void log(User admin, String action, String targetType, Long targetId, String details) {
        auditLogRepository.save(AdminAuditLog.builder()
            .admin(admin)
            .action(action)
            .targetType(targetType)
            .targetId(targetId)
            .details(details)
            .build());
    }
}
