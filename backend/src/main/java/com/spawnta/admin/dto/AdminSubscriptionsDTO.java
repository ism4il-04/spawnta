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
    BigDecimal monthlyRecurringRevenue,
    List<PlanDTO> plans,
    List<UserSubscriptionAdminDTO> subscriptions,
    List<TransactionDTO> recentTransactions
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

    public record TransactionDTO(
        Long id,
        String userEmail,
        BigDecimal amount,
        String currency,
        String status,
        LocalDateTime timestamp,
        String stripeId
    ) {}
}
