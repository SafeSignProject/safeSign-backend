package com.safesign.backend.domain.ocr.entity;

import com.safesign.backend.global.util.KstTime;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ocr_page")
public class OcrPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ocrPageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_result_id", nullable = false)
    private OcrResult ocrResult;

    @OneToMany(mappedBy = "ocrPage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrLine> lines = new ArrayList<>();

    @Column(nullable = false)
    private Integer pageNumber;

    @Column(precision = 10, scale = 2)
    private BigDecimal width;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Column(length = 20)
    private String unit;

    @Lob
    private String pageText;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public OcrPage(
            OcrResult ocrResult,
            Integer pageNumber,
            BigDecimal width,
            BigDecimal height,
            String unit,
            String pageText
    ) {
        this.ocrResult = ocrResult;
        this.pageNumber = pageNumber;
        this.width = width;
        this.height = height;
        this.unit = unit;
        this.pageText = pageText;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = KstTime.now();
    }
}
