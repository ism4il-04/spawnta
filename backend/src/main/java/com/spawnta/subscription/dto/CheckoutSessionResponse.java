package com.spawnta.subscription.dto;

import lombok.*;

/**
 * Response DTO containing Stripe checkout URL
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionResponse {
    private String sessionId;
    private String checkoutUrl;
    private String publishableKey;
}
