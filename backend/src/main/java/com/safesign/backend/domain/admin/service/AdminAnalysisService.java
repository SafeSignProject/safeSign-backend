package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.response.AdminUserAnalysisHistoryItem;
import com.safesign.backend.domain.admin.dto.response.AdminUserAnalysisHistoryResponse;
import com.safesign.backend.domain.admin.repository.AdminAnalysisRepository;
import java.time.LocalDateTime;

import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;

import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}