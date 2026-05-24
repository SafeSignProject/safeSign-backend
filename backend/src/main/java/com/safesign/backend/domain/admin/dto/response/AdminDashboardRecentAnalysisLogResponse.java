package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardRecentAnalysisLogResponse {

    private Long analysisId;
    private String fileName;
    private String status;
    private String userName;
    private String userCode;
    private String relativeTime;
}