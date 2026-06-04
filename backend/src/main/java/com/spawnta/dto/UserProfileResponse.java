package com.spawnta.dto;

import com.spawnta.entity.Interest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record UserProfileResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    String role,
    String bio,
    String avatarUrl,
    Set<Interest> interests,
    List<String> gallery,
    List<String> visitedCountries,
    String facebook,
    String instagram,
    String whatsapp,
    boolean profilePublic,
    LocalDateTime createdAt
) {}
