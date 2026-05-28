package ru.tbank.knowhow.model.users.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Token(
        UUID id,
        String username,
        Long userId,
        List<String> authorities,
        Instant createdAt,
        Instant expiresAt) {
}
