package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contract_recommended_special_clause")
public class ContractRecommendedSpecialClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recommendedClauseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private ContractAnalysisResult analysisResult;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public ContractRecommendedSpecialClause(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
    }

    public void setAnalysisResult(ContractAnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }
}