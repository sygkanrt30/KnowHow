package ru.tbank.knowhow.core_service.service.verification.code_storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
public class RedisCodeStorage implements CodeStorage {

    private static final String KEY_PREFIX = "contact:verify:";

    private final long keyExpirationTimeout;
    private final long blockTimeout;
    private final StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    public RedisCodeStorage(
            @Value("${spring.data.redis.storage.code.expiration.sec.key}") long keyExpirationTimeout,
            @Value("${spring.data.redis.storage.code.expiration.sec.block}") long sendCodeBlockTimeout,
            StringRedisTemplate redisTemplate) {

        this.keyExpirationTimeout = keyExpirationTimeout;
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.blockTimeout = sendCodeBlockTimeout;
    }

    @Override
    public void saveCode(String contact, String code) {
        String key = KEY_PREFIX + contact;
        valueOperations.set(key, code, Duration.ofSeconds(keyExpirationTimeout));
        log.debug("Code saved for key: {}", key);
    }

    @Override
    public Optional<String> getCode(String contact) {
        return Optional.ofNullable(valueOperations.get(KEY_PREFIX + contact));
    }

    @Override
    public void deleteCode(String contact) {
        String key = KEY_PREFIX + contact;
        redisTemplate.delete(key);
        log.trace("Code deleted for key: {}", key);
    }

    @Override
    public boolean isBlocked(String contact) {
        String key =  new StringBuilder(KEY_PREFIX).append("send-code:block:").append(contact)
                .toString();
        return redisTemplate.hasKey(key);
    }

    @Override
    public void blockForResend(String contact) {
        String key =  new StringBuilder(KEY_PREFIX).append("send-code:block:").append(contact)
                .toString();
        valueOperations.set(key, "blocked", Duration.ofSeconds(blockTimeout));
        log.trace("Code blocked for resend for key: {}", key);
    }
}
