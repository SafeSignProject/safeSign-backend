package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.response.AdminDashboardRecentAnalysisLogResponse;
import com.safesign.backend.domain.admin.dto.response.AdminDashboardRecentUserResponse;
import com.safesign.backend.domain.admin.dto.response.AdminDashboardResponse;
import com.safesign.backend.domain.admin.dto.response.AdminDashboardSummaryResponse;
import com.safesign.backend.domain.admin.repository.AdminDashboardRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final AdminDashboardRepository adminDashboardRepository;

    public AdminDashboardResponse getDashboard() {

        LocalDate today = LocalDate.now();
        LocalDateTime startAt = today.atStartOfDay();
        LocalDateTime endAt = today.plusDays(1).atStartOfDay();

        AdminDashboardSummaryResponse summary =
                AdminDashboardSummaryResponse.builder()
                        .totalUserCount(adminDashboardRepository.countUsers())
                        .userIncreaseFromYesterday(
                                adminDashboardRepository.countUsersCreatedBetween(
                                        startAt,
                                        endAt
                                )
                        )
                        .totalAnalysisCompletedCount(
                                adminDashboardRepository.countAnalysisCompleted()
                        )
                        .todayContractCount(
                                adminDashboardRepository.countTodayContracts(
                                        startAt,
                                        endAt
                                )
                        )
                        .todayContractIncreaseRate(
                                calculateTodayContractIncreaseRate(
                                        startAt,
                                        endAt
                                )
                        )
                        .build();

        return AdminDashboardResponse.builder()
                .summary(summary)
                .recentUsers(getRecentUsers())
                .recentAnalysisLogs(getRecentAnalysisLogs())
                .build();
    }

    private List<AdminDashboardRecentUserResponse> getRecentUsers() {

        return adminDashboardRepository.findRecentUsers()
                .stream()
                .map(row -> AdminDashboardRecentUserResponse.builder()
                        .userId(((Number) row[0]).longValue())
                        .name((String) row[1])
                        .email((String) row[2])
                        .providerType(toProviderLabel((String) row[3]))
                        .initial(getInitial((String) row[1]))
                        .build())
                .toList();
    }

    private List<AdminDashboardRecentAnalysisLogResponse> getRecentAnalysisLogs() {

        return adminDashboardRepository.findRecentAnalysisLogs()
                .stream()
                .map(row -> AdminDashboardRecentAnalysisLogResponse.builder()
                        .analysisId(((Number) row[0]).longValue())
                        .fileName((String) row[1])
                        .status((String) row[2])
                        .userName((String) row[3])
                        .userCode((String) row[4])
                        .relativeTime(toRelativeTime((LocalDateTime) row[5]))
                        .build())
                .toList();
    }

    private Double calculateTodayContractIncreaseRate(
            LocalDateTime todayStartAt,
            LocalDateTime todayEndAt
    ) {

        LocalDateTime yesterdayStartAt = todayStartAt.minusDays(1);
        LocalDateTime yesterdayEndAt = todayEndAt.minusDays(1);

        long todayCount = adminDashboardRepository.countTodayContracts(
                todayStartAt,
                todayEndAt
        );

        long yesterdayCount = adminDashboardRepository.countTodayContracts(
                yesterdayStartAt,
                yesterdayEndAt
        );

        if (yesterdayCount == 0) {
            if (todayCount == 0) {
                return 0.0;
            }

            return 100.0;
        }

        double rate =
                ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100.0;

        return Math.round(rate * 10.0) / 10.0;
    }

    private String toProviderLabel(String providerType) {

        if (providerType == null) {
            return "Email";
        }

        if (providerType.equals("LOCAL")) {
            return "Email";
        }

        if (providerType.equals("KAKAO")) {
            return "Kakao";
        }

        if (providerType.equals("GOOGLE")) {
            return "Google";
        }

        return "Email";
    }

    private String getInitial(String name) {

        if (name == null || name.isBlank()) {
            return "";
        }

        return name.substring(0, 1);
    }

    private String toRelativeTime(LocalDateTime targetTime) {

        if (targetTime == null) {
            return "";
        }

        Duration duration = Duration.between(
                targetTime,
                LocalDateTime.now()
        );

        long minutes = duration.toMinutes();

        if (minutes < 1) {
            return "방금 전";
        }

        if (minutes < 60) {
            return minutes + "분 전";
        }

        long hours = duration.toHours();

        if (hours < 24) {
            return hours + "시간 전";
        }

        long days = duration.toDays();

        return days + "일 전";
    }
}