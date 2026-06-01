package com.spawnta.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_achievements")
@IdClass(UserAchievement.UserAchievementId.class)
public class UserAchievement {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "badge_id")
    private Integer badgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", insertable = false, updatable = false)
    private Badge badge;

    @Column(name = "earned_at", nullable = false, updatable = false)
    private LocalDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        this.earnedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public UserAchievement() {}

    public UserAchievement(Long userId, Integer badgeId) {
        this.userId = userId;
        this.badgeId = badgeId;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Integer getBadgeId() { return badgeId; }
    public void setBadgeId(Integer badgeId) { this.badgeId = badgeId; }

    public User getUser() { return user; }
    public Badge getBadge() { return badge; }

    public LocalDateTime getEarnedAt() { return earnedAt; }

    // ── Composite Key ─────────────────────────────────────

    public static class UserAchievementId implements Serializable {
        private Long userId;
        private Integer badgeId;

        public UserAchievementId() {}

        public UserAchievementId(Long userId, Integer badgeId) {
            this.userId = userId;
            this.badgeId = badgeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserAchievementId that = (UserAchievementId) o;
            return Objects.equals(userId, that.userId) && Objects.equals(badgeId, that.badgeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, badgeId);
        }
    }
}
