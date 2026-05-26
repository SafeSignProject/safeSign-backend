package com.safesign.backend.domain.contract.client;

import com.safesign.backend.domain.contract.dto.request.AiAnalysisRequest;
import com.safesign.backend.domain.contract.dto.response.AiAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class AiAnalysisClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);

    private final RestClient restClient;

    public AiAnalysisClient(
            @Value("${ai.service.base-url:http://ai-service:8000}") String baseUrl
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(READ_TIMEOUT);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public AiAnalysisResponse analyzeContract(AiAnalysisRequest analysisRequest) {
        return restClient.post()
                .uri("/analyze_contract")
                .body(analysisRequest)
                .retrieve()
                .body(AiAnalysisResponse.class);
    }
}
