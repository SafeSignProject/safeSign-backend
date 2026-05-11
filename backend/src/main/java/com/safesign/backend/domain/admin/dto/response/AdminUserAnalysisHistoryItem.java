package com.safesign.backend.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserAnalysisHistoryItem {

    private LocalDateTime analyzedAt;

    private String fileName;

    private String status;

    private Integer riskScore;
}