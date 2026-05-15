package com.safesign.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {

    private boolean secure;
    private String sameSite;
}