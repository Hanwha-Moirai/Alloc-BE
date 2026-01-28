package com.moirai.alloc.auth.cookie;

import com.moirai.alloc.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class AuthCookieService {

    private final AuthCookieProperties props;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public ResponseCookie createAccessTokenCookie(String token) {
        return buildHttpOnlyCookie(props.getAccessTokenName(), token, jwtTokenProvider.getAccessExpSeconds());
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return buildHttpOnlyCookie(props.getRefreshTokenName(), token, jwtTokenProvider.getRefreshExpSeconds());
    }

    public ResponseCookie createCsrfTokenCookie(String token) {
        return buildCookie(props.getCsrfTokenName(), token, jwtTokenProvider.getRefreshExpSeconds(), false);
    }

    public ResponseCookie deleteAccessTokenCookie() {
        return deleteCookie(props.getAccessTokenName(), true);
    }

    public ResponseCookie deleteRefreshTokenCookie() {
        return deleteCookie(props.getRefreshTokenName(), true);
    }

    public ResponseCookie deleteCsrfTokenCookie() {
        return deleteCookie(props.getCsrfTokenName(), false);
    }

    public String newCsrfToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private ResponseCookie buildHttpOnlyCookie(String name, String value, long maxAgeSeconds) {
        return buildCookie(name, value, maxAgeSeconds, true);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds, boolean httpOnly) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path(props.getPath())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite(props.getSameSite())
                .secure(props.isSecure())
                .httpOnly(httpOnly);

        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }

        return builder.build();
    }

    private ResponseCookie deleteCookie(String name, boolean httpOnly) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .path(props.getPath())
                .maxAge(Duration.ZERO)
                .sameSite(props.getSameSite())
                .secure(props.isSecure())
                .httpOnly(httpOnly);

        if (props.getDomain() != null && !props.getDomain().isBlank()) {
            builder.domain(props.getDomain());
        }

        return builder.build();
    }
}
