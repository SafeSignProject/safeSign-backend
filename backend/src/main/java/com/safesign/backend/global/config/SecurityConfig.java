package com.safesign.backend.global.config;

import com.safesign.backend.domain.auth.oauth.CustomOAuth2UserService;
import com.safesign.backend.domain.auth.oauth.OAuth2SuccessHandler;
import com.safesign.backend.global.auth.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        // permitAll
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",

                                "/oauth2/**",
                                "/login/**",

                                "/favicon.ico",

                                "/api/v1/auth/reissue",

                                "/api/v1/admin/auth/**"
                        ).permitAll()

                        .requestMatchers("/api/v1/users/**")
                        .authenticated()

                        // 관리자 전용
                        .requestMatchers("/api/v1/admin/**")
                        .authenticated()

                        // 로그인 사용자
                        .requestMatchers("/api/v1/auth/**")
                        .authenticated()

                        .requestMatchers("/api/v1/contracts/**")
                        .authenticated()

                        .anyRequest()
                        .authenticated()
                )

                .oauth2Login(oauth2 -> oauth2

                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(
                                        customOAuth2UserService
                                )
                        )

                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}