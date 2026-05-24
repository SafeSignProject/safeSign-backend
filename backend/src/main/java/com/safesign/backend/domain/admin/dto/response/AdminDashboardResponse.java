package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminDashboardResponse {

    private AdminDashboardSummaryResponse summary;
    private List<AdminDashboardRecentUserResponse> recentUsers;
    private List<AdminDashboardRecentAnalysisLogResponse> recentAnalysisLogs;
}