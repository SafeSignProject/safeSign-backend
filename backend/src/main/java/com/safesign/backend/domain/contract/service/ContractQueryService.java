package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.ContractListItemResponse;
import com.safesign.backend.domain.contract.dto.response.ContractListResponse;
import com.safesign.backend.domain.contract.dto.response.ContractSummaryResponse;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import com.safesign.backend.domain.contract.repository.ContractAnalysisResultRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractQueryService {

    private final ContractRepository contractRepository;
    private final ContractAnalysisResultRepository contractAnalysisResultRepository;

    public ContractListResponse getContracts(Long userId, String keyword, String sort) {
        List<Contract> contracts = getContractsBySort(userId, keyword, sort);

        List<ContractListItemResponse> items = contracts.stream()
                .map(this::toContractListItem)
                .toList();

        items = sortItems(items, sort);

        return ContractListResponse.builder()
                .summary(buildSummary(items))
                .contracts(items)
                .build();
    }

    private List<Contract> getContractsBySort(Long userId, String keyword, String sort) {
        boolean blankKeyword = isBlank(keyword);

        if ("oldest".equals(sort)) {
            return blankKeyword
                    ? contractRepository.findByUser_UserIdOrderByUploadedAtAsc(userId)
                    : contractRepository.findByUser_UserIdAndTitleContainingIgnoreCaseOrderByUploadedAtAsc(userId, keyword);
        }

        return blankKeyword
                ? contractRepository.findByUser_UserIdOrderByUploadedAtDesc(userId)
                : contractRepository.findByUser_UserIdAndTitleContainingIgnoreCaseOrderByUploadedAtDesc(userId, keyword);
    }

    private List<ContractListItemResponse> sortItems(List<ContractListItemResponse> items, String sort) {
        if ("riskDesc".equals(sort)) {
            return items.stream()
                    .sorted((a, b) -> Integer.compare(
                            b.getRiskScore() == null ? -1 : b.getRiskScore(),
                            a.getRiskScore() == null ? -1 : a.getRiskScore()
                    ))
                    .toList();
        }

        if ("riskAsc".equals(sort)) {
            return items.stream()
                    .sorted((a, b) -> Integer.compare(
                            a.getRiskScore() == null ? 101 : a.getRiskScore(),
                            b.getRiskScore() == null ? 101 : b.getRiskScore()
                    ))
                    .toList();
        }

        return items;
    }

    private ContractListItemResponse toContractListItem(Contract contract) {
        ContractAnalysisResult analysisResult = contractAnalysisResultRepository
                .findTopByContract_ContractIdOrderByCreatedAtDesc(contract.getContractId())
                .orElse(null);

        return ContractListItemResponse.builder()
                .contractId(contract.getContractId())
                .title(contract.getTitle())
                .uploadedAt(contract.getUploadedAt())
                .analyzedAt(analysisResult == null ? null : analysisResult.getCreatedAt())
                .riskScore(analysisResult == null ? null : analysisResult.getOverallRiskScore())
                .riskCount(analysisResult == null ? 0 : countRiskClauses(analysisResult))
                .build();
    }

    private long countRiskClauses(ContractAnalysisResult analysisResult) {
        return analysisResult.getClauseAnalyses().stream()
                .filter(clause -> clause.getRiskScore() != null && clause.getRiskScore() >= 40)
                .count();
    }

    private ContractSummaryResponse buildSummary(List<ContractListItemResponse> items) {
        long high = items.stream()
                .filter(item -> item.getRiskScore() != null && item.getRiskScore() >= 70)
                .count();

        long medium = items.stream()
                .filter(item -> item.getRiskScore() != null
                        && item.getRiskScore() >= 40
                        && item.getRiskScore() < 70)
                .count();

        long low = items.stream()
                .filter(item -> item.getRiskScore() != null && item.getRiskScore() < 40)
                .count();

        return ContractSummaryResponse.builder()
                .total(items.size())
                .high(high)
                .medium(medium)
                .low(low)
                .build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}