package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.auth.service.RefreshTokenService;
import com.safesign.backend.global.auth.jwt.JwtTokenProvider;
import com.safesign.backend.global.config.CookieProperties;

import org.springframework.http.ResponseCookie;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final CookieProperties cookieProperties;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = Long.valueOf(oauth2User.getName());

        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.saveRefreshToken(
                userId,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .sameSite(cookieProperties.getSameSite())
                .maxAge(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        response.sendRedirect("http://localhost:8080/swagger-ui/index.html");
        //response.sendRedirect(frontendUrl + "/oauth/success");
    }
}