package com.spawnta.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AdminModerationDTO(
    long openUserReports,
    long investigatingUserReports,
    long resolvedUserReports,
    long dismissedUserReports,
    long openActivityReports,
    long investigatingActivityReports,
    long resolvedActivityReports,
    long dismissedActivityReports,
    List<UserReportDTO> userReports,
    List<ActivityReportDTO> activityReports
) {
    public record UserReportDTO(
        Long id,
        String status,
        String reason,
        String description,
        String reporterEmail,
        Long reportedUserId,
        String reportedUserEmail,
        String reportedUserName,
        String resolutionNotes,
        String resolvedByEmail,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
    ) {}

    public record ActivityReportDTO(
        Long id,
        String status,
        String reason,
        String description,
        String reporterEmail,
        Long activityId,
        String activityTitle,
        String hostEmail,
        String resolutionNotes,
        String resolvedByEmail,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
    ) {}
}
