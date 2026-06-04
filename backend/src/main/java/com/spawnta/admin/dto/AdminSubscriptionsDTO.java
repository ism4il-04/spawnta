package com.spawnta.admin.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record AdminSubscriptionsDTO(
    long totalPlans,
    long activeSubscriptions,
    long pendingSubscriptions,
    long pastDueSubscriptions,
    long cancelledSubscriptions,
    BigDecimal successfulPayments,
    List<PlanDTO> plans,
    List<UserSubscriptionAdminDTO> subscriptions
) {
    public record PlanDTO(
        Long id,
        String tier,
        String name,
        String description,
        BigDecimal monthlyPrice,
        Set<String> features
    ) {}

    public record UserSubscriptionAdminDTO(
        Long id,
        Long userId,
        String userEmail,
        String userName,
        String tier,
        String planName,
        String status,
        LocalDateTime startDate,
        LocalDateTime renewalDate,
        LocalDateTime endDate
    ) {}
}
