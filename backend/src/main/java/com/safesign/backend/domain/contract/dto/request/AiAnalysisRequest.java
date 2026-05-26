package com.safesign.backend.domain.contract.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.safesign.backend.domain.contract.dto.response.ClauseResponse;
import com.safesign.backend.domain.contract.dto.response.ParsingResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@AllArgsConstructor
public class AiAnalysisRequest {

    private static final Pattern ARTICLE_NO_PATTERN =
            Pattern.compile("^(제\\s*\\d+\\s*조|특약\\s*\\d+|특약사항)");

    private static final Pattern ARTICLE_TITLE_PATTERN =
            Pattern.compile("\\[([^]]+)]");

    private Long contractId;

    private List<ClauseRequest> clauses;

    public static AiAnalysisRequest from(ParsingResponse parsingResponse) {
        List<ClauseRequest> clauses = parsingResponse.getClauses()
                .stream()
                .map(AiAnalysisRequest::toClauseRequest)
                .toList();

        return new AiAnalysisRequest(
                parsingResponse.getContractId(),
                clauses
        );
    }

    private static ClauseRequest toClauseRequest(ClauseResponse clause) {
        String title = clause.getTitle();

        return new ClauseRequest(
                extractArticleNo(title),
                extractArticleTitle(title),
                clause.getContent()
        );
    }

    private static String extractArticleNo(String title) {
        Matcher matcher = ARTICLE_NO_PATTERN.matcher(title);
        return matcher.find() ? matcher.group(1).replaceAll("\\s+", "") : title;
    }

    private static String extractArticleTitle(String title) {
        Matcher matcher = ARTICLE_TITLE_PATTERN.matcher(title);
        return matcher.find() ? matcher.group(1).trim() : title;
    }

    @Getter
    @AllArgsConstructor
    public static class ClauseRequest {

        @JsonProperty("article_no")
        private String articleNo;

        @JsonProperty("article_title")
        private String articleTitle;

        @JsonProperty("raw_text")
        private String rawText;
    }
}