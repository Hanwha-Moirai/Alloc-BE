package com.moirai.alloc.common.security.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    private String refreshKey(String userId) {
        return "refresh:" + userId;
    }

    // 저장
    public void save(String userId, String refreshToken, long ttlSeconds) {
        String key = refreshKey(userId);
        redisTemplate.opsForValue().set(key, refreshToken, ttlSeconds, TimeUnit.SECONDS);
        log.info("Redis 저장 완료 → {}, token={}", key, refreshToken);
    }

    // 조회
    public String get(String userId) {
        return redisTemplate.opsForValue().get(refreshKey(userId));
    }

    // 삭제
    public void delete(String userId) {
        redisTemplate.delete(refreshKey(userId));
    }
}
