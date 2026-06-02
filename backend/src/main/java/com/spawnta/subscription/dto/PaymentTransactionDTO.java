package com.spawnta.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO for payment transaction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionDTO {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private String receiptUrl;
    private LocalDateTime createdAt;
}
