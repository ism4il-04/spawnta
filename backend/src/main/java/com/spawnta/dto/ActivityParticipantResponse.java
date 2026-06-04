package com.spawnta.dto;

import com.spawnta.entity.ParticipationStatus;

import java.time.LocalDateTime;

public record ActivityParticipantResponse(
    Long id,
    Long userId,
    String firstName,
    String lastName,
    String email,
    ParticipationStatus status,
    String introMessage,
    LocalDateTime joinedAt
) {}
