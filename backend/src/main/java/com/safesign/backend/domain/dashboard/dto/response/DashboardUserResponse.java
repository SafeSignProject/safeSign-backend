package com.safesign.backend.domain.dashboard.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardUserResponse {

    private Long userId;
    private String name;
}