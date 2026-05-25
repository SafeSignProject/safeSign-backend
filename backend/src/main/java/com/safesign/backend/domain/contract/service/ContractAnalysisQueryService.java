package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.ContractAnalysisResponse;
import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import com.safesign.backend.domain.contract.entity.ContractClauseAnalysis;
import com.safesign.backend.domain.contract.repository.ContractAnalysisResultRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractAnalysisQueryService {

    private final ContractRepository contractRepository;
    private final ContractAnalysisResultRepository analysisResultRepository;

    public ContractAnalysisResponse getAnalysisResult(Long userId, Long contractId) {
        contractRepository.findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        ContractAnalysisResult result = analysisResultRepository
                .findTopByContract_ContractIdOrderByCreatedAtDesc(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.ANALYSIS_RESULT_NOT_FOUND));

        return ContractAnalysisResponse.builder()
                .contractId(contractId)
                .analyzedAt(result.getCreatedAt())
                .overallAnalysis(ContractAnalysisResponse.OverallAnalysisResponse.builder()
                        .overallRiskScore(result.getOverallRiskScore())
                        .riskTypes(split(result.getRiskTypes()))
                        .summary(result.getSummary())
                        .build())
                .recommendedSpecialClauses(result.getRecommendedSpecialClauses()
                        .stream()
                        .map(clause -> ContractAnalysisResponse.RecommendedSpecialClauseResponse.builder()
                                .title(clause.getTitle())
                                .content(clause.getContent())
                                .build())
                        .toList())
                .clauseAnalyses(result.getClauseAnalyses()
                        .stream()
                        .map(this::toClauseAnalysisResponse)
                        .toList())
                .build();
    }

    private ContractAnalysisResponse.ClauseAnalysisResponse toClauseAnalysisResponse(
            ContractClauseAnalysis analysis
    ) {
        return ContractAnalysisResponse.ClauseAnalysisResponse.builder()
                .articleNo(analysis.getArticleNo())
                .title(analysis.getTitle())
                .clauseType(analysis.getClauseType())
                .content(analysis.getContent())
                .riskTypes(split(analysis.getRiskType()))
                .riskScore(analysis.getRiskScore())
                .relatedClauses(split(analysis.getRelatedClauses()))
                .reason(analysis.getReason())
                .relatedLaws(analysis.getRelatedLaws()
                        .stream()
                        .map(law -> ContractAnalysisResponse.RelatedLawResponse.builder()
                                .lawName(law.getLawName())
                                .article(law.getArticle())
                                .build())
                        .toList())
                .build();
    }

    private List<String> split(String values) {
        if (values == null || values.isBlank()) {
            return List.of();
        }

        return Arrays.stream(values.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
