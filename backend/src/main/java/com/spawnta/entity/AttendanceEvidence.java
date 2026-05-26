package com.spawnta.entity;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_evidence")
public class AttendanceEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private ActivityAttendance attendance;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geolocation;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────

    public AttendanceEvidence() {}

    public AttendanceEvidence(ActivityAttendance attendance, String photoUrl, Point geolocation) {
        this.attendance = attendance;
        this.photoUrl = photoUrl;
        this.geolocation = geolocation;
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ActivityAttendance getAttendance() { return attendance; }
    public void setAttendance(ActivityAttendance attendance) { this.attendance = attendance; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Point getGeolocation() { return geolocation; }
    public void setGeolocation(Point geolocation) { this.geolocation = geolocation; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
}
