package com.safesign.backend.domain.ocr.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OcrPageResponse {
    private Integer pageNumber;
    private String pageText;
    private List<OcrLineResponse> lines;
}