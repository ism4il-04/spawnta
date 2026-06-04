package com.spawnta.dto;

import com.spawnta.entity.Interest;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record UpdateProfileRequest(
    @Size(max = 500)
    String bio,

    @Size(max = 255)
    String facebook,

    @Size(max = 255)
    String instagram,

    @Size(max = 50)
    String whatsapp,

    List<String> visitedCountries,

    boolean profilePublic
) {}
