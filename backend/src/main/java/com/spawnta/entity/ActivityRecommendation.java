package com.spawnta.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_recommendations",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_id"}))
public class ActivityRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(length = 100)
    private String reason;

    @Column(name = "recommended_at", nullable = false, updatable = false)
    private LocalDateTime recommendedAt;

    @Column(nullable = false)
    private boolean clicked = false;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @PrePersist
    protected void onCreate() {
        this.recommendedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public ActivityRecommendation() {}

    public ActivityRecommendation(User user, Activity activity, BigDecimal score, String reason) {
        this.user = user;
        this.activity = activity;
        this.score = score;
        this.reason = reason;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getRecommendedAt() { return recommendedAt; }

    public boolean isClicked() { return clicked; }
    public void setClicked(boolean clicked) { this.clicked = clicked; }

    public LocalDateTime getClickedAt() { return clickedAt; }
    public void setClickedAt(LocalDateTime clickedAt) { this.clickedAt = clickedAt; }
}
