package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AnalysisStatusResponse {

    private Long contractId;
    private String status;
    private String message;
    private LocalDateTime analyzedAt;
}