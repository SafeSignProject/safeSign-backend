package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import com.safesign.backend.domain.contract.entity.ContractClause;
import com.safesign.backend.domain.contract.entity.ContractClauseAnalysis;
import com.safesign.backend.domain.contract.entity.ContractClauseRelatedLaw;
import com.safesign.backend.domain.contract.entity.ContractRecommendedSpecialClause;
import com.safesign.backend.domain.contract.enums.ContractStatus;
import com.safesign.backend.domain.contract.repository.ContractAnalysisResultRepository;
import com.safesign.backend.domain.contract.repository.ContractClauseRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractAnalysisPersistenceService {

    private final ContractRepository contractRepository;
    private final ContractClauseRepository contractClauseRepository;
    private final ContractAnalysisResultRepository analysisResultRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessing(Long userId, Long contractId) {
        Contract contract = getContract(userId, contractId);
        contract.updateStatus(ContractStatus.AI_PROCESSING);
        contract.updateFailureReason(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long userId, Long contractId) {
        Contract contract = getContract(userId, contractId);
        contract.updateStatus(ContractStatus.AI_FAILED);
        contract.updateFailureReason("AI analysis process failed.");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCompletedResult(Long userId, Long contractId, AiAnalysisResponse response) {
        Contract contract = getContract(userId, contractId);
        AiAnalysisResponse.OverallAnalysis overall = response.getOverallAnalysis();

        ContractAnalysisResult result = ContractAnalysisResult.builder()
                .contract(contract)
                .overallRiskScore(resolveRiskScore(overall))
                .riskTypes(join(overall.getRiskTypes()))
                .summary(overall.getSummary())
                .analysisTimeSeconds(toSeconds(response.getProcessingTimeMs()))
                .build();

        for (AiAnalysisResponse.RecommendedSpecialClause clause :
                safe(response.getRecommendedSpecialClauses())) {
            result.addRecommendedSpecialClause(ContractRecommendedSpecialClause.builder()
                    .title(clause.getTitle())
                    .content(clause.getContent())
                    .build());
        }

        List<AiAnalysisResponse.ClauseAnalysis> clauses = safe(response.getClauseAnalyses());
        for (int index = 0; index < clauses.size(); index++) {
            AiAnalysisResponse.ClauseAnalysis clause = clauses.get(index);
            ContractClause originalClause = findOrCreateContractClause(contract, clause, index);
            ContractClauseAnalysis analysis = ContractClauseAnalysis.builder()
                    .originalClause(originalClause)
                    .articleNo(clause.getArticleNo())
                    .title(clause.getTitle())
                    .clauseType(clause.getClauseType())
                    .content(clause.getContent())
                    .riskType(join(clause.getRiskType()))
                    .riskScore(clause.getRiskScore())
                    .relatedClauses(join(clause.getRelatedClauses()))
                    .reason(clause.getReason())
                    .build();

            for (AiAnalysisResponse.RelatedLaw law : safe(clause.getRelatedLaws())) {
                analysis.addRelatedLaw(ContractClauseRelatedLaw.builder()
                        .lawName(law.getLawName())
                        .article(law.getArticle())
                        .build());
            }

            result.addClauseAnalysis(analysis);
        }

        analysisResultRepository.save(result);
        contract.updateStatus(ContractStatus.AI_COMPLETED);
        contract.updateFailureReason(null);
    }

    private ContractClause findOrCreateContractClause(
            Contract contract,
            AiAnalysisResponse.ClauseAnalysis clause,
            int index
    ) {
        return contractClauseRepository
                .findFirstByContract_ContractIdAndClauseNoAndClauseText(
                        contract.getContractId(),
                        clause.getArticleNo(),
                        clause.getContent())
                .orElseGet(() -> contractClauseRepository.save(ContractClause.builder()
                        .contract(contract)
                        .clauseNo(clause.getArticleNo())
                        .clauseTitle(clause.getTitle())
                        .clauseText(clause.getContent())
                        .clauseType(clause.getClauseType())
                        .orderNo(index + 1)
                        .build()));
    }

    private Contract getContract(Long userId, Long contractId) {
        return contractRepository.findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));
    }

    private Integer resolveRiskScore(AiAnalysisResponse.OverallAnalysis overall) {
        return overall.getPercentile() != null
                ? overall.getPercentile()
                : overall.getOverallRiskScore();
    }

    private String join(List<String> values) {
        return values == null || values.isEmpty() ? null : String.join(",", values);
    }

    private Double toSeconds(Long processingTimeMs) {
        return processingTimeMs == null ? null : processingTimeMs / 1000.0;
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
