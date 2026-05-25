package com.spawnta.dto;

import java.time.LocalDateTime;

public record MessageResponse(
    Long id,
    Long chatId,
    Long senderId,
    String senderName,
    String senderAvatarUrl,
    String content,
    String status,
    LocalDateTime createdAt
) {}
