package com.safesign.backend.domain.ocr.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ocr_line")
public class OcrLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocrLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_page_id", nullable = false)
    private OcrPage ocrPage;

    @Column(nullable = false)
    private Integer lineNo;

    @Lob
    @Column(nullable = false)
    private String content;

    @Lob
    private String polygonJson;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public OcrLine(
            OcrPage ocrPage,
            Integer lineNo,
            String content,
            String polygonJson
    ) {
        this.ocrPage = ocrPage;
        this.lineNo = lineNo;
        this.content = content;
        this.polygonJson = polygonJson;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}