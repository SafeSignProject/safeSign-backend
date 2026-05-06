package com.safesign.backend.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(
            Long userId,
            String refreshToken,
            long expirationMillis
    ) {
        String key = "refresh:" + userId;

        stringRedisTemplate.opsForValue().set(
                key,
                refreshToken,
                Duration.ofMillis(expirationMillis)
        );
    }

    public String getRefreshToken(Long userId) {
        return stringRedisTemplate.opsForValue().get("refresh:" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        stringRedisTemplate.delete("refresh:" + userId);
    }

    public boolean matches(Long userId, String refreshToken) {
        String savedToken = getRefreshToken(userId);
        return savedToken != null && savedToken.equals(refreshToken);
    }
}