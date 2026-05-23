package com.safesign.backend.domain.admin.controller;

import com.safesign.backend.domain.admin.dto.response.AdminDashboardResponse;
import com.safesign.backend.domain.admin.service.AdminDashboardService;
import com.safesign.backend.global.auth.CustomUserDetails;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        validateAdmin(userDetails);

        return ResponseEntity.ok(
                adminDashboardService.getDashboard()
        );
    }

    private void validateAdmin(CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (!userDetails.getRole().equals("ADMIN")) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}