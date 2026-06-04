package ru.tbank.knowhow.core_service.aspect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
enum LoggingAspectMDCContextKey {

    ARGS("methodArgs"),

    SLOW_METHOD_NAME("slowMethodName"),
    SLOW_METHOD_CLASS("slowMethodClass"),

    METHOD_DURATION_MS("methodDurationMs"),
    METHOD_RESULT("methodResult"),

    EXECUTION_TIME_MS("executionTimeMs"),
    EXECUTION_METHOD("executionMethod"),
    EXECUTION_CLASS("executionClass");

    private final String value;
}
