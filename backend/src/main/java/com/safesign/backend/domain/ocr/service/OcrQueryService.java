package com.safesign.backend.domain.ocr.service;

import com.safesign.backend.domain.ocr.dto.response.OcrLineResponse;
import com.safesign.backend.domain.ocr.dto.response.OcrPageResponse;
import com.safesign.backend.domain.ocr.dto.response.OcrResponse;
import com.safesign.backend.domain.ocr.entity.OcrLine;
import com.safesign.backend.domain.ocr.entity.OcrPage;
import com.safesign.backend.domain.ocr.entity.OcrResult;
import com.safesign.backend.domain.ocr.repository.OcrLineRepository;
import com.safesign.backend.domain.ocr.repository.OcrPageRepository;
import com.safesign.backend.domain.ocr.repository.OcrResultRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OcrQueryService {

    private final OcrResultRepository ocrResultRepository;
    private final OcrPageRepository ocrPageRepository;
    private final OcrLineRepository ocrLineRepository;

    public OcrResponse getLatestOcrResult(Long contractId) {
        OcrResult ocrResult = ocrResultRepository.findLatestByContractId(contractId)
                .orElseThrow(() -> new CustomException(ErrorCode.OCR_RESULT_NOT_FOUND));

        List<OcrPage> pages = ocrPageRepository.findAllByOcrResultId(ocrResult.getOcrResultId());

        List<OcrPageResponse> pageResponses = pages.stream()
                .map(this::toPageResponse)
                .toList();

        return OcrResponse.builder()
                .contractId(contractId)
                .ocrResultId(ocrResult.getOcrResultId())
                .provider(ocrResult.getProvider())
                .modelId(ocrResult.getModelId())
                .status(ocrResult.getStatus().name())
                .fullText(ocrResult.getFullText())
                .pages(pageResponses)
                .build();
    }

    private OcrPageResponse toPageResponse(OcrPage page) {
        List<OcrLine> lines = ocrLineRepository.findAllByOcrPageId(page.getOcrPageId());

        List<OcrLineResponse> lineResponses = lines.stream()
                .map(this::toLineResponse)
                .toList();

        return OcrPageResponse.builder()
                .pageNumber(page.getPageNumber())
                .pageText(page.getPageText())
                .lines(lineResponses)
                .build();
    }

    private OcrLineResponse toLineResponse(OcrLine line) {
        return OcrLineResponse.builder()
                .lineNo(line.getLineNo())
                .content(line.getContent())
                .polygonJson(line.getPolygonJson())
                .build();
    }
}