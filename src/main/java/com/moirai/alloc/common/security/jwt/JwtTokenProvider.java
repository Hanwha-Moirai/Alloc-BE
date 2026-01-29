package com.moirai.alloc.common.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.access-exp}")
    private long accessExpSeconds;

    @Value("${jwt.refresh-exp}")
    private long refreshExpSeconds;

    @Value("${jwt.internal-exp:300}")
    private long internalExpSeconds;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    // ===== Access Token =====
    public String createAccessToken(String userId, Map<String, Object> claims) {
        return createToken(
                String.valueOf(userId),
                claims,
                accessExpSeconds
        );
    }

    // ===== Refresh Token =====
    public String createRefreshToken(Long userId) {
        return createToken(
                String.valueOf(userId),
                Map.of(
                        "typ", "refresh",
                        "jti", java.util.UUID.randomUUID().toString()
                ),
                refreshExpSeconds
        );
    }

    // ===== Internal Token =====
    public String createInternalToken(String subject) {
        return createToken(
                subject,
                Map.of(
                        "typ", "internal",
                        "scope", java.util.List.of("INTERNAL")
                ),
                internalExpSeconds
        );
    }

    private String createToken(String subject, Map<String, Object> claims, long expSec) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expSec)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getSubject(token));
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getClaims(token).get("typ", String.class));
    }

    public long getRefreshExpSeconds() {
        return refreshExpSeconds;
    }

    public long getAccessExpSeconds() {
        return accessExpSeconds;
    }
}
