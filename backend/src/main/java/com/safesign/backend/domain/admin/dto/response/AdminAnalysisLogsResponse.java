package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AdminAnalysisLogsResponse {

    private AdminAnalysisStatsResponse stats;
    private List<AdminAnalysisLogResponse> logs;

    public static AdminAnalysisLogsResponse of(
            AdminAnalysisStatsResponse stats,
            List<AdminAnalysisLogResponse> logs
    ) {
        return AdminAnalysisLogsResponse.builder()
                .stats(stats)
                .logs(logs)
                .build();
    }
}