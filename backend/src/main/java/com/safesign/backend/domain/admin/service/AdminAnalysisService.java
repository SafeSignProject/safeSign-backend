package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.request.AdminAnalysisFilterCondition;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisDetailResponse;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisIssueResponse;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisLogResponse;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisLogsResponse;
import com.safesign.backend.domain.admin.dto.response.AdminAnalysisStatsResponse;
import com.safesign.backend.domain.admin.dto.response.AdminUserAnalysisHistoryItem;
import com.safesign.backend.domain.admin.dto.response.AdminUserAnalysisHistoryResponse;
import com.safesign.backend.domain.admin.repository.AdminAnalysisRepository;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAnalysisService {

    private final AdminAnalysisRepository adminAnalysisRepository;

    private final UserRepository userRepository;

    public AdminUserAnalysisHistoryResponse getUserAnalysisHistory(
            Long userId
    ) {

        User user = userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        List<Object[]> rows =
                adminAnalysisRepository.findUserAnalysisHistories(userId);

        List<AdminUserAnalysisHistoryItem> histories =
                rows.stream()
                        .map(row -> new AdminUserAnalysisHistoryItem(
                                (LocalDateTime) row[0],
                                (String) row[1],
                                (String) row[2],
                                ((Number) row[3]).intValue()
                        ))
                        .toList();

        Long totalCount =
                adminAnalysisRepository.countUserAnalysis(userId);

        return new AdminUserAnalysisHistoryResponse(
                user.getUserId(),
                user.getName(),
                totalCount,
                histories
        );
    }

    public AdminAnalysisLogsResponse getAnalysisLogs() {

        List<Object[]> rows =
                adminAnalysisRepository.findAnalysisLogs();

        List<AdminAnalysisLogResponse> logs =
                rows.stream()
                        .map(this::toLogResponse)
                        .toList();

        return AdminAnalysisLogsResponse.of(
                createStats(logs),
                logs
        );
    }

    public AdminAnalysisLogsResponse getFilteredAnalysisLogs(
            AdminAnalysisFilterCondition condition
    ) {

        List<Object[]> rows =
                adminAnalysisRepository.findFilteredAnalysisLogs(
                        condition.getStartAt(),
                        condition.getEndAt(),
                        condition.getStatusForSearch(),
                        condition.getKeywordForSearch()
                );

        List<AdminAnalysisLogResponse> logs =
                rows.stream()
                        .map(this::toLogResponse)
                        .toList();

        return AdminAnalysisLogsResponse.of(
                createStats(logs),
                logs
        );
    }

    public AdminAnalysisDetailResponse getAnalysisLogDetail(
            Long analysisId
    ) {

        Object[] row = adminAnalysisRepository
                .findAnalysisLogDetail(analysisId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.ANALYSIS_LOG_NOT_FOUND)
                );

        List<Object[]> issueRows =
                adminAnalysisRepository.findAnalysisIssues(analysisId);

        List<AdminAnalysisIssueResponse> issues =
                issueRows.stream()
                        .map(issueRow -> AdminAnalysisIssueResponse.builder()
                                .title((String) issueRow[0])
                                .description((String) issueRow[1])
                                .riskType((String) issueRow[2])
                                .build())
                        .toList();

        return AdminAnalysisDetailResponse.builder()
                .analysisId(((Number) row[0]).longValue())
                .fileName((String) row[1])
                .analyzedAt((LocalDateTime) row[2])
                .totalTimeSeconds(toDouble(row[3]))
                .riskScore(toInteger(row[4]))
                .issues(issues)
                .build();
    }

    private AdminAnalysisLogResponse toLogResponse(Object[] row) {

        return AdminAnalysisLogResponse.builder()
                .analysisId(((Number) row[0]).longValue())
                .fileName((String) row[1])
                .status((String) row[2])
                .userName((String) row[3])
                .userCode((String) row[4])
                .analyzedAt((LocalDateTime) row[5])
                .ocrTimeSeconds(toDouble(row[6]))
                .analysisTimeSeconds(toDouble(row[7]))
                .riskScore(toInteger(row[8]))
                .issueCount(toInteger(row[9]))
                .build();
    }

    private AdminAnalysisStatsResponse createStats(
            List<AdminAnalysisLogResponse> logs
    ) {

        long totalCount = logs.size();

        long successCount = logs.stream()
                .filter(log -> "SUCCESS".equals(log.getStatus()))
                .count();

        double ocrSuccessRate = totalCount == 0
                ? 0.0
                : Math.round(((double) successCount / totalCount) * 1000.0) / 10.0;

        double averageAnalysisTimeSeconds = logs.stream()
                .map(AdminAnalysisLogResponse::getAnalysisTimeSeconds)
                .filter(time -> time != null)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        averageAnalysisTimeSeconds =
                Math.round(averageAnalysisTimeSeconds * 10.0) / 10.0;

        return AdminAnalysisStatsResponse.builder()
                .ocrSuccessRate(ocrSuccessRate)
                .averageAnalysisTimeSeconds(averageAnalysisTimeSeconds)
                .totalAnalysisCount(totalCount)
                .build();
    }

    private Double toDouble(Object value) {

        if (value == null) {
            return null;
        }

        return ((Number) value).doubleValue();
    }

    private Integer toInteger(Object value) {

        if (value == null) {
            return 0;
        }

        return ((Number) value).intValue();
    }
}