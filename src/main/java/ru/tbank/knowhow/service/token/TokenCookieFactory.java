package ru.tbank.knowhow.service.token;

import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import ru.tbank.knowhow.model.Token;
import ru.tbank.knowhow.model.User;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("DataFlowIssue")
@Setter
public class TokenCookieFactory implements Function<Authentication, Token> {

    private Duration tokenTtl = Duration.ofDays(1);

    @Override
    public Token apply(Authentication authentication) {
        var now = Instant.now();
        return new Token(UUID.randomUUID(),
                authentication.getName(),
                ((User) authentication.getPrincipal()).getId(),
                getAuthorities(authentication),
                now,
                now.plus(tokenTtl));
    }

    private @NonNull List<String> getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
