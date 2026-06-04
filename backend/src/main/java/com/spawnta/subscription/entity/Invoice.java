package com.spawnta.subscription.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import com.spawnta.entity.User;

/**
 * Invoice entity - Tracks invoices from Stripe
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, unique = true)
    private String stripeInvoiceId;
    
    @Column(nullable = false)
    private BigDecimal amount;
    
    @Builder.Default
    @Column(nullable = false)
    private String currency = "EUR";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;
    
    private String pdfUrl;
    
    private String invoiceNumber;
    
    @Column(nullable = false)
    private LocalDateTime invoiceDate;
    
    private LocalDateTime dueDate;
    
    private LocalDateTime paidDate;
    
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
