package com.spawnta.dto;

public record AuthResponse(
    String token,
    String email,
    String firstName,
    String lastName,
    String role
) {}
