package com.spawnta.dto;

import com.spawnta.entity.ActivityType;
import com.spawnta.entity.ParticipationMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateActivityRequest(
    @NotBlank @Size(max = 150)
    String title,

    String description,

    @NotNull
    ActivityType activityType,

    ParticipationMode participationMode,

    Integer maxParticipants,

    @NotNull @Future
    LocalDateTime scheduledAt,

    Integer durationMinutes,

    @Size(max = 50)
    String category,

    // For MEETUP: single location
    Double latitude,
    Double longitude,

    // For TRIP: start + destination
    Double startLatitude,
    Double startLongitude,
    Double destLatitude,
    Double destLongitude,

    @Size(max = 255)
    String address
) {}
