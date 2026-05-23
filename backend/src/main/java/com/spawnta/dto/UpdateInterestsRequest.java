package com.spawnta.dto;

import com.spawnta.entity.Interest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateInterestsRequest(
    @NotNull
    @Size(max = 10, message = "You can select at most 10 interests")
    Set<Interest> interests
) {}
