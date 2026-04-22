package com.safesign.backend.domain.ocr.entity;

import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.ocr.enums.OcrStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ocr_result")
public class OcrResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocrResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(nullable = false, length = 100)
    private String modelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OcrStatus status;

    @Lob
    private String fullText;

    @Lob
    private String rawJson;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @Column(length = 300)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public OcrResult(
            Contract contract,
            String provider,
            String modelId,
            OcrStatus status,
            String fullText,
            String rawJson,
            LocalDateTime startedAt,
            LocalDateTime completedAt,
            String failureReason
    ) {
        this.contract = contract;
        this.provider = provider;
        this.modelId = modelId;
        this.status = status;
        this.fullText = fullText;
        this.rawJson = rawJson;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.failureReason = failureReason;
    }

    public void complete(String fullText, String rawJson) {
        this.fullText = fullText;
        this.rawJson = rawJson;
        this.status = OcrStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.failureReason = null;
    }

    public void fail(String failureReason) {
        this.status = OcrStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = LocalDateTime.now();
    }

    public void updateStatus(OcrStatus status) {
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}