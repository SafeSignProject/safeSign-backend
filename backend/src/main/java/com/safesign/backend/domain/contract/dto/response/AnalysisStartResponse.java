package com.safesign.backend.domain.contract.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnalysisStartResponse {

    private Long contractId;
    private String status;
    private String message;
}