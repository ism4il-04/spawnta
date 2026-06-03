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
    
    /**
     * Get SubscriptionTier from string (case-insensitive)
     * Accepts both enum name (FREE, STARTER, PROFESSIONAL) and id (free, starter, pro)
     */
    public static SubscriptionTier fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Subscription tier cannot be null");
        }
        
        String normalized = value.trim().toLowerCase();
        
        // Try to match by ID first
        for (SubscriptionTier tier : SubscriptionTier.values()) {
            if (tier.getId().equalsIgnoreCase(normalized)) {
                return tier;
            }
        }
        
        // Try to match by enum name
        for (SubscriptionTier tier : SubscriptionTier.values()) {
            if (tier.name().equalsIgnoreCase(normalized)) {
                return tier;
            }
        }
        
        throw new IllegalArgumentException("Invalid subscription tier: " + value);
    }
}
