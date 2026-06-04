package com.spawnta.subscription.dto;

import lombok.*;

/**
 * Request DTO to initiate upgrade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpgradeSubscriptionRequest {
    private String tier; // STARTER or PROFESSIONAL
    private String successUrl;
    private String cancelUrl;
}
