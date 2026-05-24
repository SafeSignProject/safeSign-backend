package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardSummaryResponse {

    private Long totalUserCount;
    private Long userIncreaseFromYesterday;
    private Long totalAnalysisCompletedCount;
    private Long todayContractCount;
    private Double todayContractIncreaseRate;
}