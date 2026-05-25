package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}))
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatParticipantStatus status = ChatParticipantStatus.ACTIVE;

    @Column(name = "notifications_enabled", nullable = false)
    private boolean notificationsEnabled = true;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────
    public ChatParticipant() {}

    public ChatParticipant(Chat chat, User user) {
        this.chat = chat;
        this.user = user;
    }

    // ─── Getters / Setters ───────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ChatParticipantStatus getStatus() { return status; }
    public void setStatus(ChatParticipantStatus status) { this.status = status; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
