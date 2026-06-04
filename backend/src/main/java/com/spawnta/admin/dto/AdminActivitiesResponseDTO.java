package com.spawnta.admin.dto;

import java.util.List;

public record AdminActivitiesResponseDTO(
        List<AdminActivityDTO> activities,
        long totalActivities,
        long upcomingActivities,
        long pastActivities
) {}
