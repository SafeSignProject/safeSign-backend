package com.safesign.backend.domain.auth.controller;

import com.safesign.backend.domain.auth.dto.response.TokenResponse;
import com.safesign.backend.domain.auth.dto.response.UserInfoResponse;
import com.safesign.backend.domain.auth.service.RefreshTokenService;
import com.safesign.backend.global.auth.CustomUserDetails;
import com.safesign.backend.global.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                new UserInfoResponse(
                        userDetails.getUserId(),
                        userDetails.getName(),
                        userDetails.getEmail(),
                        userDetails.getRole()
                )
        );
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        if (!refreshTokenService.matches(userId, refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        return ResponseEntity.ok(new TokenResponse(newAccessToken));
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}