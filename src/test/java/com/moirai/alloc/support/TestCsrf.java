package com.moirai.alloc.support;

import com.moirai.alloc.auth.cookie.AuthCookieProperties;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestCsrf {
    private TestCsrf() {
    }

    public static RequestPostProcessor csrfToken(AuthCookieProperties authCookieProperties) {
        return request -> {
            String token = "test-csrf-token";
            request.setCookies(new Cookie(authCookieProperties.getCsrfTokenName(), token));
            request.addHeader("X-CSRF-Token", token);
            return request;
        };
    }
}
