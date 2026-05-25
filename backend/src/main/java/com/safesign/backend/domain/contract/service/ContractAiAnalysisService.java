package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.client.AiAnalysisClient;
import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAiAnalysisService {

    private final AiAnalysisClient aiAnalysisClient;
    private final ContractAnalysisPersistenceService persistenceService;

    public void analyzeFromOcr(Long userId, Long contractId, String authorization) {
        persistenceService.markProcessing(userId, contractId);

        try {
            AiAnalysisResponse response =
                    aiAnalysisClient.analyzeFromOcr(contractId, authorization);

            if (response == null || response.getOverallAnalysis() == null) {
                throw new IllegalStateException("AI analysis response is empty.");
            }

            persistenceService.saveCompletedResult(userId, contractId, response);
        } catch (Exception e) {
            log.error("AI analysis failed - contractId={}", contractId, e);
            persistenceService.markFailed(userId, contractId);
            throw new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
        }
    }
}
