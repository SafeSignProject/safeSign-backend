package com.safesign.backend.domain.contract.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResponse {

    private Long contractId;

    @JsonProperty("overall_analysis")
    private OverallAnalysis overallAnalysis;

    @JsonProperty("recommended_special_clauses")
    private List<RecommendedSpecialClause> recommendedSpecialClauses = new ArrayList<>();

    @JsonProperty("clause_analyses")
    private List<ClauseAnalysis> clauseAnalyses = new ArrayList<>();

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverallAnalysis {
        @JsonProperty("overall_risk_score")
        private Integer overallRiskScore;
        @JsonProperty("risk_types")
        private List<String> riskTypes = new ArrayList<>();
        private String summary;
        private Integer percentile;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendedSpecialClause {
        private String title;
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClauseAnalysis {
        @JsonProperty("clause_id")
        private Long clauseId;
        @JsonProperty("article_no")
        private String articleNo;
        private String title;
        @JsonProperty("clause_type")
        private String clauseType;
        private String content;
        @JsonProperty("risk_type")
        private List<String> riskType = new ArrayList<>();
        @JsonProperty("risk_score")
        private Integer riskScore;
        @JsonProperty("related_clauses")
        private List<String> relatedClauses = new ArrayList<>();
        private String reason;
        @JsonProperty("related_laws")
        private List<RelatedLaw> relatedLaws = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RelatedLaw {
        @JsonProperty("law_name")
        private String lawName;
        private String article;
    }
}
