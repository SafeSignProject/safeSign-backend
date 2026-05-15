package com.safesign.backend.domain.auth.controller;

import com.safesign.backend.domain.auth.dto.response.TokenResponse;
import com.safesign.backend.domain.auth.dto.response.UserInfoResponse;
import com.safesign.backend.domain.auth.service.RefreshTokenService;

import com.safesign.backend.global.auth.CustomUserDetails;
import com.safesign.backend.global.auth.jwt.JwtTokenProvider;
import com.safesign.backend.global.config.CookieProperties;

import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseCookie;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
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
    private final CookieProperties cookieProperties;

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        refreshTokenService.deleteRefreshToken(userDetails.getUserId());
        deleteRefreshTokenCookie(response);

        return ResponseEntity.noContent().build();
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

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(cookieProperties.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}