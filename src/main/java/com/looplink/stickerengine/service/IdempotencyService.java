package com.looplink.stickerengine.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private static final String IDEMPOTENCY_KEY_PREFIX = "idempotency:tx:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Attempts to acquire a lock for the given transaction ID.
     * @return true if lock acquired (new transaction), false if already processed
     */
    public boolean tryAcquire(String transactionId) {
        String key = IDEMPOTENCY_KEY_PREFIX + transactionId;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "processing", TTL);
        return Boolean.TRUE.equals(success);
    }

    /**
     * Marks the transaction as completed in Redis.
     */
    public void markCompleted(String transactionId) {
        String key = IDEMPOTENCY_KEY_PREFIX + transactionId;
        redisTemplate.opsForValue().set(key, "completed", TTL);
    }

    /**
     * Releases the lock if transaction processing failed.
     */
    public void release(String transactionId) {
        String key = IDEMPOTENCY_KEY_PREFIX + transactionId;
        redisTemplate.delete(key);
    }

    /**
     * Checks if a transaction is already being processed or completed.
     */
    public boolean exists(String transactionId) {
        String key = IDEMPOTENCY_KEY_PREFIX + transactionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
