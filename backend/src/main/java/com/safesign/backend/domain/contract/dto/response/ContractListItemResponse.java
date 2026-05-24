package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContractListItemResponse {

    private Long contractId;
    private String title;
    private Integer riskScore;
    private long riskCount;
    private LocalDateTime analyzedAt;
    private LocalDateTime uploadedAt;
}