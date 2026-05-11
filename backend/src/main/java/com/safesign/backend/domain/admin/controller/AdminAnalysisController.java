package com.safesign.backend.domain.admin.controller;

import com.safesign.backend.domain.admin.dto.response.AdminUserAnalysisHistoryResponse;
import com.safesign.backend.domain.admin.service.AdminAnalysisService;

import com.safesign.backend.global.auth.CustomUserDetails;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analysis")
@RequiredArgsConstructor
public class AdminAnalysisController {

    private final AdminAnalysisService adminAnalysisService;

    @GetMapping("/users/{userId}/analysis-history")
    public ResponseEntity<AdminUserAnalysisHistoryResponse>
    getUserAnalysisHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        if (userDetails == null) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (!userDetails.getRole().equals("ADMIN")) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return ResponseEntity.ok(
                adminAnalysisService.getUserAnalysisHistory(userId)
        );
    }
}