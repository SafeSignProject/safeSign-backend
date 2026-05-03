package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.user.enums.ProviderType;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
    }

    @Override
    public String getProviderUserId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        return kakaoAccount == null ? null : (String) kakaoAccount.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        Map<String, Object> properties =
                (Map<String, Object>) attributes.get("properties");

        if (properties != null && properties.get("nickname") != null) {
            return (String) properties.get("nickname");
        }

        Map<String, Object> kakaoAccount =
                (Map<String, Object>) attributes.get("kakao_account");

        if (kakaoAccount == null) {
            return null;
        }

        Map<String, Object> profile =
                (Map<String, Object>) kakaoAccount.get("profile");

        return profile == null ? null : (String) profile.get("nickname");
    }
}