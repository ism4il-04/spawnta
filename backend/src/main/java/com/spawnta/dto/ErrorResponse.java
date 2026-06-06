package com.spawnta.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
    String error,
    String message,
    LocalDateTime timestamp,
    String path,
    Map<String, String> details
) {
    public ErrorResponse(String error, String message, String path) {
        this(error, message, LocalDateTime.now(), path, null);
    }
}
