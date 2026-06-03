package com.spawnta.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spawnta.admin.dto.AdminActivitiesResponseDTO;
import com.spawnta.admin.dto.DeleteActivityRequest;
import com.spawnta.admin.service.AdminActivitiesService;

@RestController
@RequestMapping("/api/admin/activities")
@PreAuthorize("hasRole('ADMIN')")
public class AdminActivitiesController {

    private final AdminActivitiesService adminActivitiesService;

    public AdminActivitiesController(AdminActivitiesService adminActivitiesService) {
        this.adminActivitiesService = adminActivitiesService;
    }

    @GetMapping
    public ResponseEntity<AdminActivitiesResponseDTO> listActivities(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(adminActivitiesService.listActivities(search, status, category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable Long id,
            @RequestBody(required = false) DeleteActivityRequest request,
            Authentication authentication
    ) {
        adminActivitiesService.deleteActivity(id, request, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
