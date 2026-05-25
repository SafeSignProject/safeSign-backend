package com.safesign.backend.domain.contract.controller;

import com.safesign.backend.domain.contract.dto.response.ParsingResponse;
import com.safesign.backend.domain.contract.service.ContractParsingService;
import com.safesign.backend.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/contracts")
public class ContractParsingController {

    private final ContractParsingService parsingService;

    @RequestMapping(
            value = "/{contractId}/parse",
            method = {RequestMethod.GET, RequestMethod.POST}
    )
    public ParsingResponse parseContract(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contractId
    ) {
        return parsingService.parse(
                userDetails.getUserId(),
                contractId
        );
    }
}