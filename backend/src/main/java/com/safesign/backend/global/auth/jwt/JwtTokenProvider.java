package com.safesign.backend.global.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // USER ACCESS TOKEN

    public String createAccessToken(Long userId) {

        Date now = new Date();

        return Jwts.builder()

                .setSubject(String.valueOf(userId))

                .claim("role", "USER")

                .claim("type", "USER")

                .setIssuedAt(now)

                .setExpiration(
                        new Date(
                                now.getTime()
                                        + accessTokenExpiration
                        )
                )

                .signWith(key, SignatureAlgorithm.HS256)

                .compact();
    }

    // ADMIN ACCESS TOKEN
    public String createAdminAccessToken(
            Long adminId,
            String role
    ) {

        Date now = new Date();

        return Jwts.builder()

                .setSubject(String.valueOf(adminId))

                .claim("role", role)

                .claim("type", "ADMIN")

                .setIssuedAt(now)

                .setExpiration(
                        new Date(
                                now.getTime()
                                        + accessTokenExpiration
                        )
                )

                .signWith(key, SignatureAlgorithm.HS256)

                .compact();
    }

    // REFRESH TOKEN
    public String createRefreshToken(Long userId) {

        Date now = new Date();

        return Jwts.builder()

                .setSubject(String.valueOf(userId))

                .setIssuedAt(now)

                .setExpiration(
                        new Date(
                                now.getTime()
                                        + refreshTokenExpiration
                        )
                )

                .signWith(key, SignatureAlgorithm.HS256)

                .compact();
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    // USER ID 추출
    public Long getUserId(String token) {

        return Long.parseLong(

                Jwts.parserBuilder()

                        .setSigningKey(key)

                        .build()

                        .parseClaimsJws(token)

                        .getBody()

                        .getSubject()
        );
    }


    // ROLE 추출
    public String getRole(String token) {

        return Jwts.parserBuilder()

                .setSigningKey(key)

                .build()

                .parseClaimsJws(token)

                .getBody()

                .get("role", String.class);
    }


    // TYPE 추출
    public String getType(String token) {

        return Jwts.parserBuilder()

                .setSigningKey(key)

                .build()

                .parseClaimsJws(token)

                .getBody()

                .get("type", String.class);
    }

    // TOKEN 검증
    public boolean validateToken(String token) {

        try {

            Jwts.parserBuilder()

                    .setSigningKey(key)

                    .build()

                    .parseClaimsJws(token);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}