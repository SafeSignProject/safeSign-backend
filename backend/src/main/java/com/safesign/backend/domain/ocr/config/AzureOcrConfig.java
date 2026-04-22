package com.safesign.backend.domain.ocr.config;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.DocumentIntelligenceClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureOcrProperties.class)
public class AzureOcrConfig {

    @Bean
    public DocumentIntelligenceClient documentIntelligenceClient(
            AzureOcrProperties properties
    ) {
        return new DocumentIntelligenceClientBuilder()
                .endpoint(properties.endpoint())
                .credential(new AzureKeyCredential(properties.key()))
                .buildClient();
    }
}