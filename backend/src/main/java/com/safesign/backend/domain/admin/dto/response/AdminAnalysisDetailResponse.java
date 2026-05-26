package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminAnalysisDetailResponse {

    private Long analysisId;
    private String fileName;
    private LocalDateTime analyzedAt;
    private Double totalTimeSeconds;
    private Integer riskScore;
    private Integer issueCount;
    private List<AdminAnalysisIssueResponse> issues;
}