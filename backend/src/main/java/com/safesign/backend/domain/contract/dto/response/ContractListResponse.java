package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ContractListResponse {

    private ContractSummaryResponse summary;
    private List<ContractListItemResponse> contracts;
}