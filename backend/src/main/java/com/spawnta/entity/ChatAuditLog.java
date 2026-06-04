package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_audit_logs")
public class ChatAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderator_id", nullable = false)
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(nullable = false, length = 100)
    private String action; // e.g. KICK, MUTE, DELETE_MESSAGE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser; // Nullable if the action doesn't target a specific user

    @Column(length = 255)
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────
    public ChatAuditLog() {}

    public ChatAuditLog(User moderator, Chat chat, String action, User targetUser, String details) {
        this.moderator = moderator;
        this.chat = chat;
        this.action = action;
        this.targetUser = targetUser;
        this.details = details;
    }

    // ─── Getters / Setters ───────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getModerator() { return moderator; }
    public void setModerator(User moderator) { this.moderator = moderator; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public User getTargetUser() { return targetUser; }
    public void setTargetUser(User targetUser) { this.targetUser = targetUser; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
