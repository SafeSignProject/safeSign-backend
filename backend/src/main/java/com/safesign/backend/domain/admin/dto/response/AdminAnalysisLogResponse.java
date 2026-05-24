package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminAnalysisLogResponse {

    private Long analysisId;
    private String fileName;
    private String status;
    private String userName;
    private String userCode;
    private LocalDateTime analyzedAt;
    private Double ocrTimeSeconds;
    private Double analysisTimeSeconds;
    private Integer riskScore;
    private Integer issueCount;
}