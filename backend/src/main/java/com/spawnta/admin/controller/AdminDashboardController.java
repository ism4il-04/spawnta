package com.spawnta.admin.controller;

import com.spawnta.admin.dto.AdminDashboardDTO;
import com.spawnta.admin.dto.AdminSubscriptionsDTO;
import com.spawnta.admin.service.AdminDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardDTO dashboard() {
        return adminDashboardService.getDashboard();
    }

    @GetMapping("/subscriptions")
    public AdminSubscriptionsDTO subscriptions() {
        return adminDashboardService.getSubscriptions();
    }
}
