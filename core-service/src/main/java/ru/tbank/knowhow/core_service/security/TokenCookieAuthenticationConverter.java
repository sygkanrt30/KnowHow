package ru.tbank.knowhow.core_service.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.tbank.knowhow.core_service.model.users.auth.Token;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class TokenCookieAuthenticationConverter implements AuthenticationConverter {

    private final static String FIND_IN_DEACTIVATED_TOKEN =
            "SELECT COUNT(*) FROM deactivated_token WHERE id = ?";

    private final Function<String, Token> tokenCookieStringDeserializer;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (Objects.isNull(request.getCookies())) {
            return null;
        }
        return Stream.of(request.getCookies())
                .filter(cookie -> cookie.getName().equals(CookieName.HOST_AUTH_TOKEN.name()))
                .findFirst()
                .map(cookie -> createAuthentication(request, cookie))
                .orElse(null);
    }

    private @Nullable Authentication createAuthentication(HttpServletRequest request, Cookie cookie) {
        String cookieValue = cookie.getValue();
        if (Objects.isNull(cookieValue) || cookieValue.isBlank()) {
            return null;
        }

        try {
            Token token = tokenCookieStringDeserializer.apply(cookieValue);
            if (Objects.isNull(token) || isTokenDeactivated(token.id())) {
                return null;
            }
            request.setAttribute(AttributeName.USER_ID.getValue(), token.userId());
            return new PreAuthenticatedAuthenticationToken(token, cookieValue);

        } catch (Exception e) {
            log.warn("Failed to deserialize token from cookie", e);
            return null;
        }
    }

    private boolean isTokenDeactivated(UUID tokenId) {
        Long count = jdbcTemplate.queryForObject(FIND_IN_DEACTIVATED_TOKEN, Long.class, tokenId);
        return Objects.nonNull(count) && count > 0;
    }
}
