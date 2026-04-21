package com.safesign.backend.domain.ocr.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OcrResponse {
    private Long contractId;
    private Long ocrResultId;
    private String provider;
    private String modelId;
    private String status;
    private String fullText;
    private List<OcrPageResponse> pages;
}