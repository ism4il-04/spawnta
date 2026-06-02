package com.spawnta.admin.controller;

import com.spawnta.admin.dto.AdminModerationDTO;
import com.spawnta.admin.dto.ResolveReportRequest;
import com.spawnta.admin.service.AdminModerationService;
import com.spawnta.moderation.entity.ReportStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/moderation")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final AdminModerationService adminModerationService;

    public AdminModerationController(AdminModerationService adminModerationService) {
        this.adminModerationService = adminModerationService;
    }

    @GetMapping("/reports")
    public AdminModerationDTO reports(@RequestParam(defaultValue = "all") String status) {
        return adminModerationService.getReports(status);
    }

    @PatchMapping("/user-reports/{id}/investigate")
    public AdminModerationDTO.UserReportDTO investigateUserReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateUserReport(id, ReportStatus.INVESTIGATING, request, authentication);
    }

    @PatchMapping("/user-reports/{id}/resolve")
    public AdminModerationDTO.UserReportDTO resolveUserReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateUserReport(id, ReportStatus.RESOLVED, request, authentication);
    }

    @PatchMapping("/user-reports/{id}/dismiss")
    public AdminModerationDTO.UserReportDTO dismissUserReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateUserReport(id, ReportStatus.DISMISSED, request, authentication);
    }

    @PatchMapping("/activity-reports/{id}/investigate")
    public AdminModerationDTO.ActivityReportDTO investigateActivityReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateActivityReport(id, ReportStatus.INVESTIGATING, request, authentication);
    }

    @PatchMapping("/activity-reports/{id}/resolve")
    public AdminModerationDTO.ActivityReportDTO resolveActivityReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateActivityReport(id, ReportStatus.RESOLVED, request, authentication);
    }

    @PatchMapping("/activity-reports/{id}/dismiss")
    public AdminModerationDTO.ActivityReportDTO dismissActivityReport(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveReportRequest request,
            Authentication authentication) {
        return adminModerationService.updateActivityReport(id, ReportStatus.DISMISSED, request, authentication);
    }
}
