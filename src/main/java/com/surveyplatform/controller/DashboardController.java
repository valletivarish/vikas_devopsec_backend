package com.surveyplatform.controller;

import com.surveyplatform.dto.DashboardDTO;
import com.surveyplatform.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// REST controller providing dashboard analytics data
// Returns summary cards, chart data, and recent activity for the authenticated user
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Dashboard analytics and summary endpoints")
public class DashboardController {

    private final DashboardService dashboardService;

    // Constructor injection for the dashboard service
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // Get dashboard summary data including survey counts, response totals, and charts
    @GetMapping
    @Operation(summary = "Get dashboard analytics for the authenticated user")
    public ResponseEntity<DashboardDTO> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getDashboard(authentication.getName()));
    }
}
