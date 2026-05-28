package com.safesign.backend.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AdminUserAnalysisHistoryResponse {

    private Long userId;

    private String name;

    private Long totalAnalysisCount;

    private Long allUserTotalAnalysisCount;

    private List<AdminUserAnalysisHistoryItem> histories;
}
