package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contract_analysis_result")
public class ContractAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysisResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;



    @Column(nullable = false)
    private Integer overallRiskScore;

    @Column(columnDefinition = "TEXT")
    private String riskTypes;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractRecommendedSpecialClause> recommendedSpecialClauses = new ArrayList<>();

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractClauseAnalysis> clauseAnalyses = new ArrayList<>();

    @Builder
    public ContractAnalysisResult(
            Contract contract,
            Integer overallRiskScore,
            String riskTypes,
            String summary
    ) {
        this.contract = contract;
        this.overallRiskScore = overallRiskScore;
        this.riskTypes = riskTypes;
        this.summary = summary;
    }

    public void addRecommendedSpecialClause(ContractRecommendedSpecialClause clause) {
        recommendedSpecialClauses.add(clause);
        clause.setAnalysisResult(this);
    }

    public void addClauseAnalysis(ContractClauseAnalysis clauseAnalysis) {
        clauseAnalyses.add(clauseAnalysis);
        clauseAnalysis.setAnalysisResult(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}