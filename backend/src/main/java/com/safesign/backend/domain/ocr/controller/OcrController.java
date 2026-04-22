package com.safesign.backend.domain.ocr.controller;

import com.safesign.backend.domain.ocr.dto.response.OcrResponse;
import com.safesign.backend.domain.ocr.service.OcrQueryService;
import com.safesign.backend.domain.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class OcrController {

    private final OcrService ocrService;
    private final OcrQueryService ocrQueryService;

    @PostMapping("/{contractId}/ocr")
    public ResponseEntity<String> processOcr(@PathVariable Long contractId) {
        ocrService.process(contractId);
        return ResponseEntity.ok("OCR 처리가 완료되었습니다.");
    }

    @GetMapping("/{contractId}/ocr")
    public ResponseEntity<OcrResponse> getOcrResult(@PathVariable Long contractId) {
        return ResponseEntity.ok(ocrQueryService.getLatestOcrResult(contractId));
    }
}