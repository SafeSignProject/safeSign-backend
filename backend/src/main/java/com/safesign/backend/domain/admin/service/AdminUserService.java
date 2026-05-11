package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.response.AdminUserResponse;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import com.safesign.backend.domain.admin.dto.response.AdminUserDeleteResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> getUsers() {

        List<User> users = userRepository.findByDeletedAtIsNull();

        return users.stream()
                .map(user -> new AdminUserResponse(
                        user.getUserId(),
                        user.getEmail(),
                        user.getName(),
                        user.getProviderType().name(),
                        user.getRole().name(),
                        user.getCreatedAt()
                ))
                .toList();
    }

    public AdminUserResponse getUserDetail(Long userId) {

        User user = userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        return new AdminUserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getProviderType().name(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
    @Transactional
    public AdminUserDeleteResponse deleteUser(Long userId) {

        User user = userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        user.softDelete();

        return new AdminUserDeleteResponse(
                user.getUserId(),
                user.getName(),
                "회원 삭제가 완료되었습니다."
        );
    }
}