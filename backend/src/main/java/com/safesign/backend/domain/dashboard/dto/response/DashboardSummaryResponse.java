package com.safesign.backend.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardSummaryResponse {

    private Long totalContracts;
    private Long riskyContracts;
    private Long monthlyAnalyses;
}