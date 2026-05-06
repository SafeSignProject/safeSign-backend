package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.auth.service.RefreshTokenService;
import com.safesign.backend.global.auth.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        Long userId = Long.valueOf(oauth2User.getName());

        String accessToken = jwtTokenProvider.createAccessToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.saveRefreshToken(
                userId,
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false); // 로컬은 false, 배포 HTTPS는 true
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));

        response.addCookie(refreshTokenCookie);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = "{\"accessToken\": \"" + accessToken + "\"}";
        response.getWriter().write(body);
    }
}