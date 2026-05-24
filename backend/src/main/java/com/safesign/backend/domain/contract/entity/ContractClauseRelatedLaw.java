package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contract_clause_related_law")
public class ContractClauseRelatedLaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long relatedLawId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clause_analysis_id", nullable = false)
    private ContractClauseAnalysis clauseAnalysis;

    @Column(nullable = false)
    private String lawName;

    @Column(nullable = false)
    private String article;

    @Builder
    public ContractClauseRelatedLaw(
            String lawName,
            String article
    ) {
        this.lawName = lawName;
        this.article = article;
    }

    public void setClauseAnalysis(ContractClauseAnalysis clauseAnalysis) {
        this.clauseAnalysis = clauseAnalysis;
    }
}