package com.spawnta.subscription.dto;

import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO for user's current subscription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionDTO {
    private Long id;
    private SubscriptionPlanDTO plan;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime renewalDate;
    private boolean isActive;
}
