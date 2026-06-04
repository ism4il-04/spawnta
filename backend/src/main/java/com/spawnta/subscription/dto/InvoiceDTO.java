package com.spawnta.subscription.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO for invoice
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDTO {
    private Long id;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String pdfUrl;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
}
