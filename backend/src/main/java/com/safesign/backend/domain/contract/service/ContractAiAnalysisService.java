package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.client.AiAnalysisClient;
import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAiAnalysisService {

    private final AiAnalysisClient aiAnalysisClient;
    private final ContractAnalysisPersistenceService persistenceService;

    @Value("${ai.debug.expose-error:false}")
    private boolean exposeAiError;

    public void analyzeFromOcr(Long userId, Long contractId, String authorization) {
        persistenceService.markProcessing(userId, contractId);

        try {
            AiAnalysisResponse response =
                    aiAnalysisClient.analyzeFromOcr(contractId, authorization);

            if (response == null || response.getOverallAnalysis() == null) {
                throw new IllegalStateException("AI analysis response is empty.");
            }

            persistenceService.saveCompletedResult(userId, contractId, response);
        } catch (RestClientResponseException e) {
            log.error(
                    "AI analysis API failed - contractId={}, status={}, responseBody={}",
                    contractId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );
            persistenceService.markFailed(userId, contractId);
            throw toAnalysisException(
                    "AI API " + e.getStatusCode() + ": " + e.getResponseBodyAsString()
            );
        } catch (Exception e) {
            log.error("AI analysis failed - contractId={}", contractId, e);
            persistenceService.markFailed(userId, contractId);
            throw toAnalysisException(e.getMessage());
        }
    }

    private CustomException toAnalysisException(String detail) {
        if (exposeAiError && detail != null && !detail.isBlank()) {
            return new CustomException(ErrorCode.AI_ANALYSIS_FAILED, detail);
        }

        return new CustomException(ErrorCode.AI_ANALYSIS_FAILED);
    }
}
