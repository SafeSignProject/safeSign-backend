package com.safesign.backend.domain.auth.oauth;

import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.enums.UserRole;
import com.safesign.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId =
                userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oauth2User.getAttributes()
        );

        String name = userInfo.getName();
        String finalName = (name == null || name.isBlank())
                ? "사용자"
                : name;

        User user = userRepository.findByProviderTypeAndProviderUserId(
                userInfo.getProviderType(),
                userInfo.getProviderUserId()
        ).orElseGet(() -> userRepository.save(
                User.builder()
                        .email(userInfo.getEmail())
                        .name(finalName)
                        .providerType(userInfo.getProviderType())
                        .providerUserId(userInfo.getProviderUserId())
                        .role(UserRole.USER)
                        .build()
        ));

        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("userId", user.getUserId());

        return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                "userId"
        );
    }
}