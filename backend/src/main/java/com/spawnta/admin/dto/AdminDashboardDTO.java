package com.spawnta.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardDTO(
    long totalUsers,
    long admins,
    long premiumUsers,
    long bannedUsers,
    long suspendedUsers,
    long unverifiedUsers,
    long totalActivities,
    long upcomingActivities,
    long openUserReports,
    long openActivityReports,
    long subscriptionPlans,
    long activeSubscriptions,
    BigDecimal successfulPayments,
    List<AuditEntryDTO> recentAuditLogs,
    List<PlatformActivityDTO> recentActivities
) {
    public record AuditEntryDTO(
        Long id,
        String adminEmail,
        String action,
        String targetType,
        Long targetId,
        String details,
        LocalDateTime createdAt
    ) {}

    public record PlatformActivityDTO(
        String type, // USER_JOIN, ACTIVITY_CREATE, NEW_REPORT
        String title,
        String description,
        String iconType,
        LocalDateTime timestamp
    ) {}
}
