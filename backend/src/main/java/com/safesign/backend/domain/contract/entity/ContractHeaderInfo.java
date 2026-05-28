package com.safesign.backend.domain.contract.entity;

import com.safesign.backend.global.util.KstTime;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    @Column(name = "header_info_id")
    private Long headerInfoId;

    // 계약 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    // 임대인명
    @Column(name = "landlord_name", length = 100)
    private String landlordName;

    // 주소
    @Column(name = "property_address", length = 500)
    private String propertyAddress;

    // 면적
    @Column(name = "property_area", precision = 10, scale = 2)
    private BigDecimal propertyArea;

    // 보증금
    @Column(name = "deposit_amount")
    private Long depositAmount;

    // 월세
    @Column(name = "monthly_rent")
    private Long monthlyRent;

    // 계약금
    @Column(name = "contract_payment")
    private Long contractPayment;

    // 중도금
    @Column(name = "intermediate_payment")
    private Long intermediatePayment;

    // 잔금
    @Column(name = "balance_payment")
    private Long balancePayment;

    // 중개보수
    @Column(name = "broker_fee")
    private Long brokerFee;

    // 생성일
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = KstTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        this.updatedAt = KstTime.now();
    }
}
