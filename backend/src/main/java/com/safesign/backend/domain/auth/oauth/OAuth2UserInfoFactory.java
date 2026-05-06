package com.safesign.backend.domain.auth.oauth;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(
            String registrationId,
            java.util.Map<String, Object> attributes
    ) {
        return switch (registrationId) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }
}