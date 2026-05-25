package com.safesign.backend.domain.ocr.controller;

import com.safesign.backend.domain.contract.service.ContractAiAnalysisService;
import com.safesign.backend.domain.contract.service.ContractParsingService;
import com.safesign.backend.domain.ocr.dto.response.OcrResponse;
import com.safesign.backend.domain.ocr.service.OcrQueryService;
import com.safesign.backend.domain.ocr.service.OcrService;
import com.safesign.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class OcrController {

    private final OcrService ocrService;
    private final OcrQueryService ocrQueryService;
    private final ContractParsingService parsingService;
    private final ContractAiAnalysisService contractAiAnalysisService;

    @PostMapping("/{contractId}/ocr")
    public ResponseEntity<String> processOcr(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contractId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization
    ) {
        // 1. OCR 처리 및 OCR 결과 DB 저장
        ocrService.process(userDetails.getUserId(), contractId);

        // 2. OCR 결과 기반 조항 파싱 실행
        parsingService.parse(
                userDetails.getUserId(),
                contractId
        );

        // 3. AI 분석 요청 및 AI 결과 DB 저장
        contractAiAnalysisService.analyzeFromOcr(
                userDetails.getUserId(),
                contractId,
                authorization
        );

        return ResponseEntity.ok("OCR, 파싱 및 AI 분석 처리가 완료되었습니다.");
    }

    @GetMapping("/{contractId}/ocr")
    public ResponseEntity<OcrResponse> getOcrResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contractId
    ) {
        OcrResponse response = ocrQueryService.getLatestOcrResult(
                userDetails.getUserId(),
                contractId
        );

        return ResponseEntity.ok(response);
    }
}