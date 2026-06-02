package com.spawnta.subscription.entity;

/**
 * Enum representing invoice statuses
 */
public enum InvoiceStatus {
    /**
     * Invoice has been paid
     */
    PAID,
    
    /**
     * Invoice is in draft state
     */
    DRAFT,
    
    /**
     * Invoice is open and awaiting payment
     */
    OPEN,
    
    /**
     * Invoice payment could not be collected
     */
    UNCOLLECTIBLE,
    
    /**
     * Invoice has been voided
     */
    VOID
}
