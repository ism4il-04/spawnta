package com.spawnta.dto;

public record MyActivityResponse(
    ActivityResponse activity,
    ActivityParticipationStatusDto participation
) {}
