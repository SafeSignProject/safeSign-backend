package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.client.AiAnalysisClient;
import com.safesign.backend.domain.contract.dto.request.AiAnalysisRequest;
import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import com.safesign.backend.domain.contract.dto.response.ParsingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAiAnalysisWorker {

    private final ContractParsingQueryService parsingQueryService;
    private final AiAnalysisClient aiAnalysisClient;
    private final ContractAnalysisPersistenceService persistenceService;

    @Async("aiAnalysisExecutor")
    public void analyzeAsync(Long userId, Long contractId) {
        try {
            log.info("AI async analysis started - contractId={}", contractId);

            // 이미 DB에 저장된 파싱 결과 조회
            ParsingResponse parsingResponse =
                    parsingQueryService.getParsedResult(userId, contractId);

            AiAnalysisRequest request =
                    AiAnalysisRequest.from(parsingResponse);

            log.info(
                    "AI async request sending - contractId={}, clauseCount={}",
                    contractId,
                    request.getClauses().size()
            );

            AiAnalysisResponse response =
                    aiAnalysisClient.analyzeContract(request);

            if (response == null || response.getOverallAnalysis() == null) {
                throw new IllegalStateException("AI analysis response is empty.");
            }

            log.info(
                    "AI async response received - contractId={}, overallRiskScore={}, percentile={}, clauseCount={}",
                    contractId,
                    response.getOverallAnalysis().getOverallRiskScore(),
                    response.getOverallAnalysis().getPercentile(),
                    response.getClauseAnalyses() == null ? 0 : response.getClauseAnalyses().size()
            );

            persistenceService.saveCompletedResult(userId, contractId, response);

            log.info("AI async analysis result saved - contractId={}", contractId);

        } catch (RestClientResponseException e) {
            log.error(
                    "AI async API failed - contractId={}, status={}, responseBody={}",
                    contractId,
                    e.getStatusCode(),
                    e.getResponseBodyAsString(),
                    e
            );

            persistenceService.markFailed(userId, contractId);

        } catch (Exception e) {
            log.error("AI async analysis failed - contractId={}", contractId, e);

            persistenceService.markFailed(userId, contractId);
        }
    }
}