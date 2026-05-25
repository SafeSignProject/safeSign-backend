package com.safesign.backend.domain.contract.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ContractAnalysisResponse {

    private Long contractId;
    private LocalDateTime analyzedAt;
    private OverallAnalysisResponse overallAnalysis;
    private List<RecommendedSpecialClauseResponse> recommendedSpecialClauses;
    private List<ClauseAnalysisResponse> clauseAnalyses;

    @Getter
    @Builder
    public static class OverallAnalysisResponse {

        private Integer overallRiskScore;
        private List<String> riskTypes;
        private String summary;
    }

    @Getter
    @Builder
    public static class RecommendedSpecialClauseResponse {

        private String title;
        private String content;
    }

    @Getter
    @Builder
    public static class ClauseAnalysisResponse {

        private String articleNo;
        private String title;
        private String clauseType;
        private String content;
        private List<String> riskTypes;
        private Integer riskScore;
        private List<String> relatedClauses;
        private String reason;
        private List<RelatedLawResponse> relatedLaws;
    }

    @Getter
    @Builder
    public static class RelatedLawResponse {

        private String lawName;
        private String article;
    }
}
