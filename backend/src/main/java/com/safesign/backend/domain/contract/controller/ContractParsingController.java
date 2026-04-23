package com.safesign.backend.domain.contract.controller;

import com.safesign.backend.domain.contract.service.ContractParsingService;
import com.safesign.backend.domain.contract.dto.response.ParsingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractParsingController {

    private final ContractParsingService parsingService;

    @PostMapping("/{contractId}/parse")
    public ParsingResponse parseContract(@PathVariable Long contractId) {
        return parsingService.parse(contractId);
    }
}