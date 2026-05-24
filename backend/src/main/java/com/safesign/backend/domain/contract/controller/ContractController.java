package com.safesign.backend.domain.contract.controller;

import com.safesign.backend.domain.contract.dto.response.ContractUploadResponse;
import com.safesign.backend.domain.contract.dto.response.ContractListResponse;
import com.safesign.backend.domain.contract.enums.UploadType;
import com.safesign.backend.domain.contract.service.ContractService;
import com.safesign.backend.domain.contract.service.ContractQueryService;
import com.safesign.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractQueryService contractQueryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ContractUploadResponse uploadContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("title") String title,
            @RequestParam("uploadType") UploadType uploadType
    ) {
        Long userId = userDetails.getUserId();

        return contractService.uploadContract(userId, files, title, uploadType);
    }

    @GetMapping
    public ContractListResponse getContracts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = userDetails.getUserId();

        return contractQueryService.getContracts(userId, keyword, sort);
    }

    @DeleteMapping("/{contractId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contractId
    ) {
        Long userId = userDetails.getUserId();

        contractService.deleteContract(userId, contractId);
    }
}