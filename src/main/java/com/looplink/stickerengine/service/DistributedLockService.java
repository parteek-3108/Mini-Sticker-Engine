package com.looplink.stickerengine.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class DistributedLockService {

    private static final String LOCK_KEY_PREFIX = "lock:";
    private static final Duration DEFAULT_LOCK_TTL = Duration.ofSeconds(30);

    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> unlockScript;

    public DistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.unlockScript = new DefaultRedisScript<>();
        this.unlockScript.setScriptText(UNLOCK_SCRIPT);
        this.unlockScript.setResultType(Long.class);
    }

    /**
     * Attempts to acquire a distributed lock.
     * @param lockKey the key to lock on (e.g., shopper ID)
     * @return lock token if acquired, null if lock not available
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_LOCK_TTL);
    }

    /**
     * Attempts to acquire a distributed lock with custom TTL.
     * @param lockKey the key to lock on
     * @param ttl lock expiration time
     * @return lock token if acquired, null if lock not available
     */
    public String tryLock(String lockKey, Duration ttl) {
        String key = LOCK_KEY_PREFIX + lockKey;
        String token = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
        return Boolean.TRUE.equals(success) ? token : null;
    }

    /**
     * Releases a distributed lock.
     * Only releases if the token matches (prevents releasing someone else's lock).
     * @param lockKey the key that was locked
     * @param token the token returned from tryLock
     * @return true if lock was released, false if lock was not held or token mismatch
     */
    public boolean unlock(String lockKey, String token) {
        if (token == null) {
            return false;
        }
        String key = LOCK_KEY_PREFIX + lockKey;
        Long result = redisTemplate.execute(unlockScript, Collections.singletonList(key), token);
        return result != null && result > 0;
    }

    /**
     * Executes an action while holding a lock.
     * @param lockKey the key to lock on
     * @param action the action to execute
     * @return the result of the action
     * @throws LockAcquisitionException if lock cannot be acquired
     */
    public <T> T executeWithLock(String lockKey, LockAction<T> action) {
        String token = tryLock(lockKey);
        if (token == null) {
            throw new LockAcquisitionException("Failed to acquire lock for: " + lockKey);
        }
        try {
            return action.execute();
        } finally {
            unlock(lockKey, token);
        }
    }

    @FunctionalInterface
    public interface LockAction<T> {
        T execute();
    }

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }
    }
}
