package com.spawnta.admin.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;
import com.spawnta.entity.User;

/**
 * AdminAuditLog entity - Tracks all admin actions for auditing
 */
@Entity
@Table(name = "admin_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;
    
    @Column(nullable = false)
    private String action;
    
    @Column(nullable = false)
    private String targetType; // USER, ACTIVITY, REPORT, etc.
    
    private Long targetId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    private String ipAddress;
    
    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
