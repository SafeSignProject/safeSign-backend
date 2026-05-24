package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContractSummaryResponse {

    private long total;
    private long high;
    private long medium;
    private long low;
}