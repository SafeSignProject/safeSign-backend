package com.safesign.backend.domain.admin.service;

import com.safesign.backend.domain.admin.dto.request.AdminLoginRequest;
import com.safesign.backend.domain.admin.dto.response.AdminLoginResponse;

import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.enums.UserRole;
import com.safesign.backend.domain.user.repository.UserRepository;

import com.safesign.backend.global.auth.jwt.JwtTokenProvider;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;

    public AdminLoginResponse login(
            AdminLoginRequest request
    ) {

        User user = userRepository
                .findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        // 관리자 체크
        if (user.getRole() != UserRole.ADMIN) {

            throw new CustomException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        // 비밀번호 체크
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {

            throw new CustomException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        String accessToken =
                jwtTokenProvider.createAdminAccessToken(
                        user.getUserId(),
                        user.getRole().name()
                );

        return new AdminLoginResponse(
                accessToken,
                user.getRole().name(),
                user.getName()
        );
    }
}