package com.spawnta.subscription.entity;

/**
 * Enum representing different subscription tiers
 */
public enum SubscriptionTier {
    /**
     * Free tier - limited features
     * - Maximum 5 activities per week
     * - Basic profile
     * - Standard search
     */
    FREE("free"),
    
    /**
     * Starter tier - entry-level premium
     * - Unlimited activities
     * - Enhanced profile
     * - Advanced search
     * - Priority support
     */
    STARTER("starter"),
    
    /**
     * Professional tier - full premium features
     * - Unlimited everything
     * - Analytics dashboard
     * - Premium support
     * - API access
     */
    PROFESSIONAL("pro");
    
    private final String id;
    
    SubscriptionTier(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
}
