package com.safesign.backend.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardRecentUserResponse {

    private Long userId;
    private String name;
    private String email;
    private String providerType;
    private String initial;
}