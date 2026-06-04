package com.spawnta.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_ratings",
       uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "rater_id"}))
public class ActivityRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    @Column(name = "rating_score", nullable = false)
    private Integer ratingScore;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public ActivityRating() {}

    public ActivityRating(Activity activity, User rater, Integer ratingScore, String comment) {
        this.activity = activity;
        this.rater = rater;
        this.ratingScore = ratingScore;
        this.comment = comment;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }

    public User getRater() { return rater; }
    public void setRater(User rater) { this.rater = rater; }

    public Integer getRatingScore() { return ratingScore; }
    public void setRatingScore(Integer ratingScore) {
        if (ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5");
        }
        this.ratingScore = ratingScore;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
