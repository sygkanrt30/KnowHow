package ru.tbank.knowhow.core_service.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttributeName {
    USER_ID("userId");

    private final String value;
}
