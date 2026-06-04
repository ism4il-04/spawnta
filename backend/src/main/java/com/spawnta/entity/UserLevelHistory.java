package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_level_history")
public class UserLevelHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "old_level")
    private Integer oldLevel;

    @Column(name = "new_level")
    private Integer newLevel;

    @Column(name = "xp_at_time")
    private Integer xpAtTime;

    @Column(name = "achieved_at", nullable = false, updatable = false)
    private LocalDateTime achievedAt;

    @PrePersist
    protected void onCreate() {
        this.achievedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public UserLevelHistory() {}

    public UserLevelHistory(User user, Integer oldLevel, Integer newLevel, Integer xpAtTime) {
        this.user = user;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.xpAtTime = xpAtTime;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getOldLevel() { return oldLevel; }
    public void setOldLevel(Integer oldLevel) { this.oldLevel = oldLevel; }

    public Integer getNewLevel() { return newLevel; }
    public void setNewLevel(Integer newLevel) { this.newLevel = newLevel; }

    public Integer getXpAtTime() { return xpAtTime; }
    public void setXpAtTime(Integer xpAtTime) { this.xpAtTime = xpAtTime; }

    public LocalDateTime getAchievedAt() { return achievedAt; }
}
