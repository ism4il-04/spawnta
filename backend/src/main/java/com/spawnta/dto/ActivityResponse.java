package com.spawnta.dto;

import com.spawnta.entity.ActivityType;
import com.spawnta.entity.ParticipationMode;

import java.time.LocalDateTime;

public record ActivityResponse(
    Long id,
    String title,
    String description,
    ActivityType activityType,
    ParticipationMode participationMode,
    Integer maxParticipants,
    LocalDateTime scheduledAt,
    Integer durationMinutes,
    String category,

    // Coordinates as doubles for JSON serialization
    Double latitude,
    Double longitude,
    Double startLatitude,
    Double startLongitude,
    Double destLatitude,
    Double destLongitude,
    String address,

    // Host info
    Long hostId,
    String hostFirstName,
    String hostLastName,
    String hostEmail,

    int participantCount,
    LocalDateTime createdAt
) {}
