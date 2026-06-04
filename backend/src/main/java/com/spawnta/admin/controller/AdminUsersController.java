package com.spawnta.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spawnta.entity.Role;
import com.spawnta.admin.dto.AdminUserDTO;
import com.spawnta.admin.dto.AdminUsersResponseDTO;
import com.spawnta.admin.dto.ModerateUserRequest;
import com.spawnta.admin.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

    private final AdminUserService adminUserService;

    public AdminUsersController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<AdminUsersResponseDTO> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tier
    ) {
        return ResponseEntity.ok(adminUserService.listUsers(search, status, tier));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<AdminUserDTO> updateRole(
            @PathVariable Long id,
            @RequestParam Role role,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserService.updateRole(id, role, authentication.getName()));
    }

    @PatchMapping("/{id}/ban")
    public ResponseEntity<AdminUserDTO> banUser(
            @PathVariable Long id,
            @Valid @RequestBody ModerateUserRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserService.banUser(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<AdminUserDTO> suspendUser(
            @PathVariable Long id,
            @Valid @RequestBody ModerateUserRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserService.suspendUser(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<AdminUserDTO> restoreUser(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminUserService.restoreUser(id, authentication.getName()));
    }
}
