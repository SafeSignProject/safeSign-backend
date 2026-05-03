package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.user.enums.ProviderType;

public interface OAuth2UserInfo {
    ProviderType getProviderType();
    String getProviderUserId();
    String getEmail();
    String getName();
}