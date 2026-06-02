package com.spawnta.admin.dto;

public record AdminUsersSummaryDTO(
        long totalUsers,
        long admins,
        long premiumUsers,
        long bannedUsers,
        long suspendedUsers,
        long unverifiedUsers
) {}
