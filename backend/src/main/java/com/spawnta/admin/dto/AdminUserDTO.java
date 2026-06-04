package com.spawnta.admin.dto;

import java.time.LocalDateTime;

import com.spawnta.entity.Role;

public record AdminUserDTO(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role,
        boolean emailVerified,
        String subscriptionTier,
        boolean premium,
        boolean banned,
        LocalDateTime suspendedUntil,
        String suspensionReason,
        Integer level,
        Integer xp,
        LocalDateTime createdAt
) {}
