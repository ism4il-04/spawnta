package com.spawnta.subscription.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import com.spawnta.entity.User;

/**
 * UserSubscription entity - Tracks user's subscription status and Stripe references
 * One-to-one relationship with User
 */
@Entity
@Table(name = "user_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    
    @Column(nullable = false)
    private String stripeCustomerId;
    
    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
    
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private LocalDateTime renewalDate;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
    
    @Column(name = "cancel_reason")
    private String cancelReason;
    
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
    
    /**
     * Check if subscription is currently active
     */
    public boolean isActive() {
        return SubscriptionStatus.ACTIVE.equals(status) && 
               (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Check if subscription needs renewal
     */
    public boolean needsRenewal() {
        return renewalDate != null && renewalDate.isBefore(LocalDateTime.now()) && isActive();
    }
}
