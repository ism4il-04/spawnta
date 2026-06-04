package com.spawnta.admin.dto;

import java.util.List;

public record AdminUsersResponseDTO(
        List<AdminUserDTO> users,
        AdminUsersSummaryDTO summary
) {}
