package com.spawnta.subscription.dto;

import lombok.*;

/**
 * Request DTO to cancel subscription
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelSubscriptionRequest {
    private String reason;
}
