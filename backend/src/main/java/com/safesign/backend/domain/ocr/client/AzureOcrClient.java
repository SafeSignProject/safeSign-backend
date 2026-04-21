package com.safesign.backend.domain.ocr.client;

import com.azure.ai.documentintelligence.DocumentIntelligenceClient;
import com.azure.ai.documentintelligence.models.AnalyzeDocumentOptions;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AzureOcrClient {

    private final DocumentIntelligenceClient documentIntelligenceClient;

    public AnalyzeResult analyze(byte[] fileBytes, String modelId) {
        AnalyzeDocumentOptions options =
                new AnalyzeDocumentOptions(BinaryData.fromBytes(fileBytes));

        SyncPoller<?, AnalyzeResult> poller =
                documentIntelligenceClient.beginAnalyzeDocument(modelId, options);

        return poller.getFinalResult();
    }
}