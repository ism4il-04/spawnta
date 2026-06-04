package com.spawnta.moderation.entity;

/**
 * Enum representing report statuses
 */
public enum ReportStatus {
    /**
     * Report is newly created and awaiting review
     */
    OPEN,
    
    /**
     * Report is being investigated
     */
    INVESTIGATING,
    
    /**
     * Report has been resolved
     */
    RESOLVED,
    
    /**
     * Report was dismissed without action
     */
    DISMISSED
}
