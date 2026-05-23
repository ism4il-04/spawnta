package com.spawnta.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Manages opaque refresh tokens stored in Redis.
 * Key pattern: refresh:<token>  →  value: email
 */
@Service
public class RefreshTokenService {

    private static final String PREFIX = "refresh:";

    private final StringRedisTemplate redis;
    private final long refreshExpirySeconds;

    public RefreshTokenService(
        StringRedisTemplate redis,
        @Value("${app.jwt.refresh-expiry:604800000}") long refreshExpiryMs
    ) {
        this.redis = redis;
        this.refreshExpirySeconds = refreshExpiryMs / 1000;
    }

    /** Creates a new refresh token for the given email and persists it in Redis. */
    public String createToken(String email) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(PREFIX + token, email, Duration.ofSeconds(refreshExpirySeconds));
        return token;
    }

    /**
     * Validates the refresh token.
     * @return the email stored for this token, or throws if invalid / expired.
     */
    public String validateAndGetEmail(String token) {
        String email = redis.opsForValue().get(PREFIX + token);
        if (email == null) {
            throw new IllegalArgumentException("Refresh token invalid or expired");
        }
        return email;
    }

    /** Revokes a specific refresh token (logout). */
    public void revoke(String token) {
        redis.delete(PREFIX + token);
    }

    /** Revokes all refresh tokens for a given email (force-logout all sessions). */
    public void revokeAll(String email) {
        var keys = redis.keys(PREFIX + "*");
        if (keys == null) return;
        keys.stream()
            .filter(k -> email.equals(redis.opsForValue().get(k)))
            .forEach(redis::delete);
    }
}
