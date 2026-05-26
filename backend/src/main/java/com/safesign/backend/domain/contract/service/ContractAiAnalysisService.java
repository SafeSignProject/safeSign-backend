package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.AnalysisStartResponse;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAiAnalysisService {

    private final ContractRepository contractRepository;
    private final ContractAnalysisPersistenceService persistenceService;
    private final ContractAiAnalysisWorker analysisWorker;

    public AnalysisStartResponse startAnalysis(Long userId, Long contractId) {
        contractRepository.findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        // 분석 상태를 PROCESSING으로 먼저 저장
        persistenceService.markProcessing(userId, contractId);

        // 실제 AI 호출은 비동기로 실행
        analysisWorker.analyzeAsync(userId, contractId);

        log.info("AI analysis accepted - contractId={}", contractId);

        return new AnalysisStartResponse(
                contractId,
                "PROCESSING",
                "AI 분석이 시작되었습니다."
        );
    }
}