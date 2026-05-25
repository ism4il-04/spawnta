package com.spawnta.dto;

import java.time.LocalDateTime;

public record ChatResponse(
    Long id,
    String type,
    Long activityId,
    String activityTitle,
    String status,
    LocalDateTime createdAt,
    String title,
    String avatarUrl,
    String lastMessage,
    LocalDateTime lastMessageTime,
    String lastMessageSender,
    boolean notificationsEnabled
) {}
