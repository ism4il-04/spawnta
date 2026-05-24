package com.spawnta.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 20)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_mode", nullable = false, length = 20)
    private ParticipationMode participationMode = ParticipationMode.DIRECT;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(length = 50)
    private String category;

    // ─── Spatial columns ─────────────────────────────────
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;          // for MEETUP

    @Column(name = "start_location", columnDefinition = "geometry(Point,4326)")
    private Point startLocation;     // for TRIP

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point destination;       // for TRIP

    @Column(length = 255)
    private String address;

    // ─── Relations ───────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityParticipant> participants = new ArrayList<>();

    // ─── Timestamps ──────────────────────────────────────
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Constructors ────────────────────────────────────
    public Activity() {}

    // ─── Getters / Setters ───────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ActivityType getActivityType() { return activityType; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }

    public ParticipationMode getParticipationMode() { return participationMode; }
    public void setParticipationMode(ParticipationMode participationMode) { this.participationMode = participationMode; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public Point getStartLocation() { return startLocation; }
    public void setStartLocation(Point startLocation) { this.startLocation = startLocation; }

    public Point getDestination() { return destination; }
    public void setDestination(Point destination) { this.destination = destination; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }

    public List<ActivityParticipant> getParticipants() { return participants; }
    public void setParticipants(List<ActivityParticipant> participants) { this.participants = participants; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
