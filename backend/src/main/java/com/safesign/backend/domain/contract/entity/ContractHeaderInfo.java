package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contract_header_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractHeaderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long headerInfoId;

    // 🔗 계약 FK (연관관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // 원본 표제부
    @Column(columnDefinition = "TEXT")
    private String rawHeader;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String landCategory;

    @Column(columnDefinition = "TEXT")
    private String landArea;

    @Column(columnDefinition = "TEXT")
    private String buildingStructure;

    @Column(columnDefinition = "TEXT")
    private String buildingUsage;

    @Column(columnDefinition = "TEXT")
    private String buildingArea;

    @Column(columnDefinition = "TEXT")
    private String leasedPart;

    @Column(columnDefinition = "TEXT")
    private String leasedArea;

    @Column(columnDefinition = "TEXT")
    private String deposit;

    @Column(columnDefinition = "TEXT")
    private String contractAmount;

    @Column(columnDefinition = "TEXT")
    private String middlePayment;

    @Column(columnDefinition = "TEXT")
    private String balance;

    @Column(columnDefinition = "TEXT")
    private String monthlyRent;
}