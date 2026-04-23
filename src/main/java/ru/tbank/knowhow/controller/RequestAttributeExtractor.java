package ru.tbank.knowhow.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static ru.tbank.knowhow.security.AttributeName.USER_ID;

@Slf4j
class RequestAttributeExtractor {

    static Long extractUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(USER_ID.getValue());
        if (Objects.isNull(userId)) {
            log.debug("Cannot extract user id from request attribute");
        }
        return userId;
    }
}
