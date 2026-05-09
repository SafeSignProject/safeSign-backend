package com.safesign.backend.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponse {

    private String accessToken;

    private String role;

    private String name;
}