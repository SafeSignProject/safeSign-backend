package com.safesign.backend.domain.admin.controller;

import com.safesign.backend.domain.admin.dto.response.AdminUserResponse;
import com.safesign.backend.domain.admin.service.AdminUserService;

import com.safesign.backend.global.auth.CustomUserDetails;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @AuthenticationPrincipal
            CustomUserDetails userDetails
    ) {

        // 로그인 안 된 경우
        if (userDetails == null) {

            throw new CustomException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        // 관리자 권한 체크
        if (!userDetails.getRole().equals("ADMIN")) {

            throw new CustomException(
                    ErrorCode.ACCESS_DENIED
            );
        }

        return ResponseEntity.ok(
                adminUserService.getUsers()
        );
    }
}