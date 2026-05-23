package com.safesign.backend.domain.admin.controller;

import com.safesign.backend.domain.admin.dto.request.AdminAnalysisFilterCondition;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisDetailResponse;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisLogsResponse;
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
    public ResponseEntity<AdminUserAnalysisHistoryResponse> getUserAnalysisHistory(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        validateAdmin(userDetails);

        return ResponseEntity.ok(
                adminAnalysisService.getUserAnalysisHistory(userId)
        );
    }

    @GetMapping("/logs")
    public ResponseEntity<AdminAnalysisLogsResponse> getAnalysisLogs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        validateAdmin(userDetails);

        return ResponseEntity.ok(
                adminAnalysisService.getAnalysisLogs()
        );
    }

    @GetMapping("/logs/filter")
    public ResponseEntity<AdminAnalysisLogsResponse> getFilteredAnalysisLogs(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        validateAdmin(userDetails);

        AdminAnalysisFilterCondition condition =
                AdminAnalysisFilterCondition.of(
                        period,
                        status,
                        keyword
                );

        return ResponseEntity.ok(
                adminAnalysisService.getFilteredAnalysisLogs(condition)
        );
    }

    @GetMapping("/logs/{analysisId}")
    public ResponseEntity<AdminAnalysisDetailResponse> getAnalysisLogDetail(
            @PathVariable Long analysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        validateAdmin(userDetails);

        return ResponseEntity.ok(
                adminAnalysisService.getAnalysisLogDetail(analysisId)
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