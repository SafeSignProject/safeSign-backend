package com.safesign.backend.domain.contract.dto.response;

import com.safesign.backend.domain.contract.enums.ContractStatus;
import com.safesign.backend.domain.contract.enums.UploadType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ContractUploadResponse {

    private Long contractId;
    private String title;
    private UploadType uploadType;
    private Integer pageCount;
    private ContractStatus status;
    private LocalDateTime uploadedAt;
}