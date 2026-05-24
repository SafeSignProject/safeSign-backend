package com.safesign.backend.domain.dashboard.controller;

import com.safesign.backend.domain.dashboard.dto.response.DashboardResponse;
import com.safesign.backend.domain.dashboard.service.DashboardService;
import com.safesign.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return dashboardService.getDashboard(userDetails.getUserId());
    }
}