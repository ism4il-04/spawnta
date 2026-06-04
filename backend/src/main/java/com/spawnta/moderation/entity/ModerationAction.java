package com.spawnta.moderation.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import com.spawnta.entity.User;

/**
 * ModerationAction entity - Records moderation actions taken by admins
 */
@Entity
@Table(name = "moderation_actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerationAction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType; // WARN, SUSPEND, BAN, RESTORE
    
    @Column(nullable = false)
    private String targetType; // USER, ACTIVITY
    
    @Column(nullable = false)
    private Long targetId;
    
    @Column(nullable = false)
    private String reason;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_id", nullable = false)
    private User initiatedBy;
    
    private LocalDateTime suspensionEndDate; // For SUSPEND actions
    
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
