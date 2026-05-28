package com.safesign.backend.domain.admin.dto.request;

import com.safesign.backend.global.util.KstTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminAnalysisFilterCondition(
        String period,
        String status,
        String keyword
) {

    public static AdminAnalysisFilterCondition of(
            String period,
            String status,
            String keyword
    ) {
        return new AdminAnalysisFilterCondition(
                normalize(period),
                normalize(status),
                normalize(keyword)
        );
    }

    public LocalDateTime getStartAt() {

        if (period == null || period.equalsIgnoreCase("ALL")) {
            return null;
        }

        LocalDate today = KstTime.today();

        if (period.equalsIgnoreCase("TODAY")) {
            return today.atStartOfDay();
        }

        if (period.equalsIgnoreCase("WEEK")) {
            return today.minusDays(6).atStartOfDay();
        }

        if (period.equalsIgnoreCase("MONTH")) {
            return today.minusDays(29).atStartOfDay();
        }

        return null;
    }

    public LocalDateTime getEndAt() {

        if (period == null || period.equalsIgnoreCase("ALL")) {
            return null;
        }

        return KstTime.today().plusDays(1).atStartOfDay();
    }

    public String getStatusForSearch() {

        if (status == null || status.equalsIgnoreCase("ALL")) {
            return null;
        }

        if (status.equalsIgnoreCase("FAIL")) {
            return "FAILED";
        }

        return status.toUpperCase();
    }

    public String getKeywordForSearch() {
        return keyword;
    }

    private static String normalize(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
