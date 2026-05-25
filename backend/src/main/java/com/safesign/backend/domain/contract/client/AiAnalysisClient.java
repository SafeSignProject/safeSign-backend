package com.safesign.backend.domain.contract.client;

import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiAnalysisClient {

    private final RestClient restClient;

    public AiAnalysisClient(
            @Value("${ai.service.base-url:http://ai-service:8000}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public AiAnalysisResponse analyzeFromOcr(Long contractId, String authorization) {
        RestClient.RequestBodySpec request = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/analyze_from_ocr/{contractId}")
                        .queryParam("explain", true)
                        .build(contractId));

        if (authorization != null && !authorization.isBlank()) {
            request.header(HttpHeaders.AUTHORIZATION, authorization);
        }

        return request.retrieve().body(AiAnalysisResponse.class);
    }
}
