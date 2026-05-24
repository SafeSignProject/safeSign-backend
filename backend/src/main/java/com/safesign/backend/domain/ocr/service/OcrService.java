package com.safesign.backend.domain.ocr.service;

import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.DocumentLine;
import com.azure.ai.documentintelligence.models.DocumentPage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractFile;
import com.safesign.backend.domain.contract.enums.ContractStatus;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.domain.contract.service.FileStorageService;
import com.safesign.backend.domain.ocr.client.AzureOcrClient;
import com.safesign.backend.domain.ocr.config.AzureOcrProperties;
import com.safesign.backend.domain.ocr.entity.OcrLine;
import com.safesign.backend.domain.ocr.entity.OcrPage;
import com.safesign.backend.domain.ocr.entity.OcrResult;
import com.safesign.backend.domain.ocr.enums.OcrStatus;
import com.safesign.backend.domain.ocr.repository.OcrLineRepository;
import com.safesign.backend.domain.ocr.repository.OcrPageRepository;
import com.safesign.backend.domain.ocr.repository.OcrResultRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OcrService {

    private static final String OCR_PROVIDER = "AZURE";

    private final ContractRepository contractRepository;
    private final OcrResultRepository ocrResultRepository;
    private final OcrPageRepository ocrPageRepository;
    private final OcrLineRepository ocrLineRepository;
    private final AzureOcrClient azureOcrClient;
    private final AzureOcrProperties azureOcrProperties;
    private final ObjectMapper objectMapper;
    private final FileStorageService fileStorageService;

    public void process(Long userId, Long contractId) {
        Contract contract = contractRepository
                .findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        ContractFile contractFile = getFirstContractFile(contract);
        contract.updateStatus(ContractStatus.OCR_PROCESSING);

        OcrResult ocrResult = OcrResult.builder()
                .contract(contract)
                .provider(OCR_PROVIDER)
                .modelId(azureOcrProperties.modelId())
                .status(OcrStatus.PROCESSING)
                .startedAt(LocalDateTime.now())
                .build();

        ocrResultRepository.save(ocrResult);

        try {
            byte[] fileBytes =
                    fileStorageService.loadFile(contractFile.getFileUrl());
            AnalyzeResult analyzeResult =
                    azureOcrClient.analyze(fileBytes, azureOcrProperties.modelId());

            ocrResult.complete(
                    analyzeResult.getContent(),
                    toJson(analyzeResult)
            );

            savePagesAndLines(ocrResult, analyzeResult);

            contract.updateStatus(ContractStatus.OCR_COMPLETED);
            contract.updateFailureReason(null);

        } catch (CustomException e) {
            ocrResult.fail(e.getMessage());
            contract.updateStatus(ContractStatus.OCR_FAILED);
            contract.updateFailureReason(e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("OCR 처리 실패 - contractId={}", contractId, e);

            ocrResult.fail(e.getMessage());
            contract.updateStatus(ContractStatus.OCR_FAILED);
            contract.updateFailureReason(e.getMessage());

            throw new CustomException(ErrorCode.OCR_PROCESS_FAILED);
        }
    }

    private ContractFile getFirstContractFile(Contract contract) {
        List<ContractFile> files = contract.getContractFiles();

        if (files == null || files.isEmpty()) {
            throw new CustomException(ErrorCode.CONTRACT_FILE_NOT_FOUND);
        }

        return files.get(0);
    }

    private void savePagesAndLines(OcrResult ocrResult, AnalyzeResult analyzeResult) {
        if (analyzeResult.getPages() == null) {
            return;
        }

        for (DocumentPage page : analyzeResult.getPages()) {
            OcrPage ocrPage = OcrPage.builder()
                    .ocrResult(ocrResult)
                    .pageNumber(page.getPageNumber())
                    .width(toBigDecimal(page.getWidth()))
                    .height(toBigDecimal(page.getHeight()))
                    .unit(page.getUnit() != null ? page.getUnit().toString() : null)
                    .pageText(extractPageText(page))
                    .build();

            ocrPageRepository.save(ocrPage);

            List<DocumentLine> lines = page.getLines();
            if (lines == null || lines.isEmpty()) {
                continue;
            }

            for (int i = 0; i < lines.size(); i++) {
                DocumentLine line = lines.get(i);

                OcrLine ocrLine = OcrLine.builder()
                        .ocrPage(ocrPage)
                        .lineNo(i + 1)
                        .content(line.getContent())
                        .polygonJson(toJson(line.getPolygon()))
                        .build();

                ocrLineRepository.save(ocrLine);
            }
        }
    }

    private String extractPageText(DocumentPage page) {
        if (page.getLines() == null || page.getLines().isEmpty()) {
            return null;
        }

        return page.getLines().stream()
                .map(DocumentLine::getContent)
                .reduce((a, b) -> a + "\n" + b)
                .orElse(null);
    }

    private BigDecimal toBigDecimal(Number value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.OCR_PROCESS_FAILED);
        }
    }
}