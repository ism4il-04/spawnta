package com.spawnta.subscription.entity;

/**
 * Enum representing payment transaction statuses
 */
public enum PaymentStatus {
    /**
     * Payment successfully completed
     */
    SUCCEEDED,
    
    /**
     * Payment failed
     */
    FAILED,
    
    /**
     * Payment is pending
     */
    PENDING,
    
    /**
     * Payment has been cancelled
     */
    CANCELLED,
    
    /**
     * Payment requires additional action (3D Secure, etc.)
     */
    REQUIRES_ACTION
}
