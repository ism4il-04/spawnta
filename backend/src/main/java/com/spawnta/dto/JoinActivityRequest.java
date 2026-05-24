package com.spawnta.dto;

import jakarta.validation.constraints.Size;

public record JoinActivityRequest(
    @Size(max = 150)
    String introMessage
) {}
