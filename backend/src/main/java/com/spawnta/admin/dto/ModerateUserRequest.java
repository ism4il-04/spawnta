package com.spawnta.admin.dto;

public record ModerateUserRequest(
        String reason,
        Integer days
) {}
