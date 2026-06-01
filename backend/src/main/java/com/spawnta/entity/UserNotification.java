package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_activity_id")
    private Activity relatedActivity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_user_id")
    private User relatedUser;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public UserNotification() {}

    public UserNotification(User user, NotificationType type, String title, String message) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Activity getRelatedActivity() { return relatedActivity; }
    public void setRelatedActivity(Activity relatedActivity) { this.relatedActivity = relatedActivity; }

    public User getRelatedUser() { return relatedUser; }
    public void setRelatedUser(User relatedUser) { this.relatedUser = relatedUser; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
}
