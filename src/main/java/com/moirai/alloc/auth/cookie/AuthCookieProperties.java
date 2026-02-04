package com.moirai.alloc.auth.cookie;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "auth.cookie")
public class AuthCookieProperties {

    private String accessTokenName = "accessToken";
    private String refreshTokenName = "refreshToken";
    private String csrfTokenName = "csrfToken";
    private String sameSite = "Lax";
    private boolean secure = false;
    private String path = "/";
    private String domain;
}
