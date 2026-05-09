package com.safesign.backend.domain.admin.controller;

import com.safesign.backend.domain.admin.dto.request.AdminLoginRequest;
import com.safesign.backend.domain.admin.dto.response.AdminLoginResponse;
import com.safesign.backend.domain.admin.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(
            @RequestBody AdminLoginRequest request
    ) {

        return ResponseEntity.ok(
                adminAuthService.login(request)
        );
    }
}