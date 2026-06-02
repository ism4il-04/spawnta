package com.spawnta.moderation.entity;

/**
 * Enum representing moderation action types
 */
public enum ActionType {
    /**
     * Issue a warning to user/activity
     */
    WARN,
    
    /**
     * Temporarily suspend access
     */
    SUSPEND,
    
    /**
     * Permanently ban user/activity
     */
    BAN,
    
    /**
     * Restore previously banned/suspended item
     */
    RESTORE
}
