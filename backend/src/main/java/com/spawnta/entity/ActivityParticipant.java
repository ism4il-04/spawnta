package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_participants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "user_id"}))
public class ActivityParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationStatus status = ParticipationStatus.PENDING;

    @Column(name = "intro_message", length = 150)
    private String introMessage;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────
    public ActivityParticipant() {}

    public ActivityParticipant(Activity activity, User user, ParticipationStatus status, String introMessage) {
        this.activity = activity;
        this.user = user;
        this.status = status;
        this.introMessage = introMessage;
    }

    // ─── Getters / Setters ───────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ParticipationStatus getStatus() { return status; }
    public void setStatus(ParticipationStatus status) { this.status = status; }

    public String getIntroMessage() { return introMessage; }
    public void setIntroMessage(String introMessage) { this.introMessage = introMessage; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
}
