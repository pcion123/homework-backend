package com.example.demo.repository;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class JobLockerDao {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
            	return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public JobLockerDao(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<String> tryLock(String id, Duration timeout) {
        String lockValue = generateLockValue();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey(id), lockValue, timeout);

        return Boolean.TRUE.equals(locked) ? Optional.of(lockValue) : Optional.empty();
    }

    public boolean unlock(String id, String lockValue) {
        Long deleted = redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey(id)), lockValue);

        return Long.valueOf(1L).equals(deleted);
    }

    private String lockKey(String id) {
        return "job:{" + id + "}";
    }

    private String generateLockValue() {
        return "%d%03d".formatted(System.currentTimeMillis(), secureRandom.nextInt(1000));
    }
}