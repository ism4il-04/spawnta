package com.spawnta.subscription.entity;

/**
 * Enum representing subscription statuses
 */
public enum SubscriptionStatus {
    /**
     * Subscription is currently active and valid
     */
    ACTIVE,
    
    /**
     * User initiated cancellation
     */
    CANCELLED,
    
    /**
     * Subscription has ended
     */
    EXPIRED,
    
    /**
     * Payment failed, subscription requires attention
     */
    PAST_DUE,
    
    /**
     * Payment is currently being processed
     */
    PENDING
}
