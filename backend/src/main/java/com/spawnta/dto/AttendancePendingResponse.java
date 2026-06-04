package com.spawnta.dto;

import java.time.LocalDateTime;

public record AttendancePendingResponse(
    Long attendanceId,
    Long userId,
    String firstName,
    String lastName,
    String email,
    LocalDateTime checkInTime
) {}
