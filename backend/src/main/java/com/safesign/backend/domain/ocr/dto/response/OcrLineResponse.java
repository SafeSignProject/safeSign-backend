package com.safesign.backend.domain.ocr.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OcrLineResponse {
    private Integer lineNo;
    private String content;
    private String polygonJson;
}