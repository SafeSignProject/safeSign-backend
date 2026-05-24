package com.safesign.backend.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DashboardResponse {

    private DashboardUserResponse user;
    private DashboardSummaryResponse summary;
    private List<RecentContractResponse> recentContracts;
}