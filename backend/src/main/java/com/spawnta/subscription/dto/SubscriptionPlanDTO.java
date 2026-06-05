package com.spawnta.subscription.dto;

import java.math.BigDecimal;
import java.util.Set;

import lombok.*;

/**
 * DTO for subscription plan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanDTO {
    private Long id;
    private String tier;
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private BigDecimal discountedPrice;
    private String discountReason;
    private Set<String> features;
}
