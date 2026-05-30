package ru.tbank.knowhow.core_service.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static ru.tbank.knowhow.core_service.security.AttributeName.USER_ID;

@Slf4j
public final class RequestAttributeExtractor {

    public static Long extractUserId(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(USER_ID.getValue());
        if (Objects.isNull(userId)) {
            log.debug("Cannot extract user id from request attribute");
        }
        return userId;
    }
}
