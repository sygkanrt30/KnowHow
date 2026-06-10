package ru.tbank.knowhow.core_service.service.verification.attempt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class VerificationAttemptLimiter implements AttemptLimiter {

    private static final String KEY_PREFIX = "contact:verify:attempt:";

    private final long keyExpiration;
    private final int attemptLimit;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final ValueOperations<String, Integer> valueOps;

    @Autowired
    public VerificationAttemptLimiter(
            @Value("${spring.data.redis.storage.attempt.expiration.sec.attempt-key}") long keyExpiration,
            @Value("${spring.data.redis.storage.attempt.limit}") int limit,
            RedisTemplate<String, Integer> redisTemplate) {

        this.keyExpiration = keyExpiration;
        this.redisTemplate = redisTemplate;
        this.attemptLimit = limit;
        this.valueOps = redisTemplate.opsForValue();
    }

    @Override
    public boolean isAttemptAllowed(String contact) {
        return getAttemptCount(contact) >= attemptLimit;
    }

    @Override
    public void registerFailedAttempt(String contact) {
        String key = KEY_PREFIX + contact;
        valueOps.increment(key);

        if (getAttemptCount(contact) == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(keyExpiration));
        }
    }

    @Override
    public void resetAttempts(String contact) {
        String key = KEY_PREFIX + contact;
        valueOps.increment(key);
    }

    @Override
    public int getAttemptCount(String contact) {
        String key = KEY_PREFIX + contact;
        Integer attempts = valueOps.get(key);
        return Objects.nonNull(attempts) ? attempts : 0;
    }
}
