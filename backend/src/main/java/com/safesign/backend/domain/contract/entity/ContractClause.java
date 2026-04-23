package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contract_clause")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clauseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    private String clauseTitle;

    @Column(columnDefinition = "TEXT")
    private String clauseText;

    private Integer orderNo;
}