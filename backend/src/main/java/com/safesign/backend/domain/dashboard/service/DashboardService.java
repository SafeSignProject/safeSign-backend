package com.safesign.backend.domain.dashboard.service;

import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import com.safesign.backend.domain.contract.repository.ContractAnalysisResultRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.domain.dashboard.dto.response.DashboardResponse;
import com.safesign.backend.domain.dashboard.dto.response.DashboardSummaryResponse;
import com.safesign.backend.domain.dashboard.dto.response.DashboardUserResponse;
import com.safesign.backend.domain.dashboard.dto.response.RecentContractResponse;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int RISKY_SCORE_THRESHOLD = 70;

    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ContractAnalysisResultRepository contractAnalysisResultRepository;

    public DashboardResponse getDashboard(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Long totalContracts = contractRepository.countByUser_UserId(userId);
        Long riskyContracts = contractAnalysisResultRepository
                .countRiskyAnalysesByUserId(userId, RISKY_SCORE_THRESHOLD);

        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        Long monthlyAnalyses = contractAnalysisResultRepository.countMonthlyAnalysesByUserId(
                userId,
                startOfMonth,
                startOfNextMonth
        );

        List<RecentContractResponse> recentContracts =
                contractAnalysisResultRepository.findRecentAnalysesByUserId(
                                userId,
                                PageRequest.of(0, 3)
                        )
                        .stream()
                        .map(this::toRecentContractResponse)
                        .toList();

        return DashboardResponse.builder()
                .user(DashboardUserResponse.builder()
                        .userId(user.getUserId())
                        .name(user.getName())
                        .build())
                .summary(DashboardSummaryResponse.builder()
                        .totalContracts(totalContracts)
                        .riskyContracts(riskyContracts)
                        .monthlyAnalyses(monthlyAnalyses)
                        .build())
                .recentContracts(recentContracts)
                .build();
    }

    private RecentContractResponse toRecentContractResponse(ContractAnalysisResult analysisResult) {
        Integer riskScore = analysisResult.getOverallRiskScore();

        return RecentContractResponse.builder()
                .contractId(analysisResult.getContract().getContractId())
                .title(analysisResult.getContract().getTitle())
                .analyzedAt(analysisResult.getCreatedAt())
                .riskScore(riskScore)
                .riskCount(countRiskTypes(analysisResult.getRiskTypes()))
                .build();
    }

    private Integer countRiskTypes(String riskTypes) {
        if (riskTypes == null || riskTypes.isBlank()) {
            return 0;
        }

        return riskTypes.split(",").length;
    }
}