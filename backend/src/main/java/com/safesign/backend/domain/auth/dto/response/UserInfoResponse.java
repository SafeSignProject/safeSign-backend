package com.safesign.backend.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String name;
    private String email;
    private String role;
}