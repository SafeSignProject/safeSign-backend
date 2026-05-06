package com.safesign.backend.domain.ocr.controller;

import com.safesign.backend.domain.ocr.dto.response.OcrResponse;
import com.safesign.backend.domain.ocr.service.OcrQueryService;
import com.safesign.backend.domain.ocr.service.OcrService;
import com.safesign.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class OcrController {

    private final OcrService ocrService;
    private final OcrQueryService ocrQueryService;

    @PostMapping("/{contractId}/ocr")
    public ResponseEntity<String> processOcr(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contractId
    ) {
        ocrService.process(userDetails.getUserId(), contractId);
        return ResponseEntity.ok("OCR 처리가 완료되었습니다.");
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