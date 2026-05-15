package com.safesign.backend.domain.user.service;

import com.safesign.backend.domain.auth.service.RefreshTokenService;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;
import com.safesign.backend.global.config.CookieProperties;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final CookieProperties cookieProperties;

    public void deleteMe(
            Long userId,
            HttpServletResponse response
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );

        user.withdraw();

        refreshTokenService.deleteRefreshToken(userId);
        deleteRefreshTokenCookie(response);
    }

    private void deleteRefreshTokenCookie(
            HttpServletResponse response
    ) {
        ResponseCookie cookie = ResponseCookie.from(
                        "refreshToken",
                        ""
                )
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite(cookieProperties.getSameSite())
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
    }
}