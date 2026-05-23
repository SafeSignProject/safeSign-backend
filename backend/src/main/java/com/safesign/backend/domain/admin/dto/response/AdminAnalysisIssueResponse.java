package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminAnalysisIssueResponse {

    private String title;
    private String description;
    private String riskType;
}