package com.spawnta.admin.dto;

import java.time.LocalDateTime;

import com.spawnta.entity.ActivityType;
import com.spawnta.entity.ParticipationMode;

public record AdminActivityDTO(
        Long id,
        String title,
        String description,
        ActivityType activityType,
        ParticipationMode participationMode,
        String category,
        Integer maxParticipants,
        int participantCount,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt,
        Long hostId,
        String hostEmail,
        String hostName
) {}
