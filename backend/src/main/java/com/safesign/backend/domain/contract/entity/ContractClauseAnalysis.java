package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contract_clause_analysis")
public class ContractClauseAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clauseAnalysisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private ContractAnalysisResult analysisResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_clause_id")
    private ContractClause originalClause;

    @Column(length = 100)
    private String articleNo;

    private String title;

    @Column(length = 100)
    private String clauseType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String riskType;

    private Integer riskScore;

    @Column(columnDefinition = "TEXT")
    private String relatedClauses;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @OneToMany(mappedBy = "clauseAnalysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractClauseRelatedLaw> relatedLaws = new ArrayList<>();

    @Builder
    public ContractClauseAnalysis(
            ContractClause originalClause,
            String articleNo,
            String title,
            String clauseType,
            String content,
            String riskType,
            Integer riskScore,
            String relatedClauses,
            String reason
    ) {
        this.originalClause = originalClause;
        this.articleNo = articleNo;
        this.title = title;
        this.clauseType = clauseType;
        this.content = content;
        this.riskType = riskType;
        this.riskScore = riskScore;
        this.relatedClauses = relatedClauses;
        this.reason = reason;
    }

    public void setAnalysisResult(ContractAnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
    }

    public void addRelatedLaw(ContractClauseRelatedLaw relatedLaw) {
        relatedLaws.add(relatedLaw);
        relatedLaw.setClauseAnalysis(this);
    }
}