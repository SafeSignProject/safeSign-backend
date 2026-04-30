package com.safesign.backend.domain.contract.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ocr_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractOcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocrResultId;

    // 계약서와 연관관계 (FK: contract_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    // OCR 전체 텍스트
    @JsonIgnore
    @Column(name = "full_text", columnDefinition = "TEXT")
    private String fullText;
}