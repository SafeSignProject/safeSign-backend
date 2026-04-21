package com.safesign.backend.domain.ocr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.document-intelligence")
public record AzureOcrProperties(
        String endpoint,
        String key,
        String modelId
) {
}