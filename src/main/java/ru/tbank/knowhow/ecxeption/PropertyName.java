package ru.tbank.knowhow.ecxeption;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
enum PropertyName {
    ERROR_CODE("errorCode"),
    TIMESTAMP("timestamp"),
    DETAILS("details"),
    PATH("path"),
    EXCEPTION_TYPE("exceptionType"),
    STATUS("status");


    private final String value;
}

