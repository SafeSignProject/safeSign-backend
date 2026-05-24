package com.safesign.backend.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentContractResponse {

    private Long contractId;
    private String title;
    private LocalDateTime analyzedAt;
    private Integer riskScore;
    private Integer riskCount;
}