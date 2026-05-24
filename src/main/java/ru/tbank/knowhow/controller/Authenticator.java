package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.tbank.knowhow.ecxeption.LoginException;
import ru.tbank.knowhow.security.TokenCookieSessionAuthenticationStrategy;

@RequiredArgsConstructor
@Component
@Slf4j
class Authenticator {

    private final TokenCookieSessionAuthenticationStrategy tokenCookieSessionAuthenticationStrategy;
    private final AuthenticationManager authenticationManager;

    void authenticateAndSetCookie(HttpServletRequest request, HttpServletResponse response,
                                         String username, byte[] password) {
        log.trace("trying to authenticate user with username ({})", username);
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, new String(password))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            tokenCookieSessionAuthenticationStrategy.onAuthentication(authentication, request, response);
        } catch (Exception e) {
            throw new LoginException("Authentication failed", e);
        }
        log.debug("successfully authenticated user with username ({})", username);
    }
}
