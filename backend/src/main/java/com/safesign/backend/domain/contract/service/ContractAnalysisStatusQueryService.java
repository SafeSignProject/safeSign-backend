package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.AnalysisStatusResponse;
import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import com.safesign.backend.domain.contract.repository.ContractAnalysisResultRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractAnalysisStatusQueryService {

    private final ContractRepository contractRepository;
    private final ContractAnalysisResultRepository analysisResultRepository;

    public AnalysisStatusResponse getAnalysisStatus(Long userId, Long contractId) {
        contractRepository.findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        ContractAnalysisResult latestResult = analysisResultRepository
                .findTopByContract_ContractIdOrderByCreatedAtDesc(contractId)
                .orElse(null);

        if (latestResult == null) {
            return AnalysisStatusResponse.builder()
                    .contractId(contractId)
                    .status("PROCESSING")
                    .message("AI 분석 중이거나 아직 분석 결과가 없습니다.")
                    .analyzedAt(null)
                    .build();
        }

        return AnalysisStatusResponse.builder()
                .contractId(contractId)
                .status("COMPLETED")
                .message("AI 분석이 완료되었습니다.")
                .analyzedAt(latestResult.getCreatedAt())
                .build();
    }
}