package ru.tbank.knowhow.service.token;

import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import ru.tbank.knowhow.model.Token;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Setter
public class TokenCookieFactory implements Function<Authentication, Token> {

    private Duration tokenTtl = Duration.ofDays(1);

    @Override
    public Token apply(Authentication authentication) {
        var now = Instant.now();
        return new Token(UUID.randomUUID(), authentication.getName(),
                (Long) authentication.getCredentials(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList(),
                now, now.plus(tokenTtl));
    }
}
