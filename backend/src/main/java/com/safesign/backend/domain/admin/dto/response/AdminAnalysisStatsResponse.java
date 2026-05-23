package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAnalysisStatsResponse {

    private Double ocrSuccessRate;
    private Double averageAnalysisTimeSeconds;
    private Long totalAnalysisCount;
}