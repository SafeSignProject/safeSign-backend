package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.user.enums.ProviderType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }

    @Override
    public String getProviderUserId() {
        return String.valueOf(attributes.get("sub"));
    }

    @Override
    public String getEmail() {
        return String.valueOf(attributes.get("email"));
    }

    @Override
    public String getName() {
        return String.valueOf(attributes.get("name"));
    }
}