package com.safesign.backend.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminUserResponse {

    private Long userId;

    private String email;

    private String name;

    private String providerType;

    private String role;

    private LocalDateTime createdAt;
}