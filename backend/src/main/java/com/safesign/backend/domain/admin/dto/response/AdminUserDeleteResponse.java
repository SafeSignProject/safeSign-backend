package com.safesign.backend.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminUserDeleteResponse {

    private Long userId;

    private String name;

    private String message;
}