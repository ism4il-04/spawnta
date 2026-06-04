package com.spawnta.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePrivateChatRequest(
    @NotNull(message = "L'identifiant du destinataire est requis")
    Long targetUserId
) {}
