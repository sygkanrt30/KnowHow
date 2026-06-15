package ru.tbank.knowhow.core_service.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.tbank.knowhow.core_service.aspect.LoggingAspectMDCContextKey.*;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    private static final int LOWER_TIME_LIMIT_FOR_SLOW_METHOD = 1000;
    private static final int RESULT_LENGHT_LIMIT = 500;
    private static final String PARAM_DELIMITER = ", ";
    private static final String TRACE_ID = "traceId";

    @Pointcut(value = """
                    execution(* ru.tbank.knowhow..*.*(..)) &&
                    !within(ru.tbank.knowhow.core_service.ecxeption..*) &&
                    !within(ru.tbank.knowhow.core_service.config..*) &&
                    !execution(* ru.tbank.knowhow.core_service..*.get*(..)) &&
                    !execution(* ru.tbank.knowhow.core_service..*.set*(..)) &&
                    !execution(* ru.tbank.knowhow.core_service..*.toString()) &&
                    !execution(* ru.tbank.knowhow.core_service..*.hashCode()) &&
                    !execution(* ru.tbank.knowhow.core_service..*.equals(..)) &&
                    !within(ru.tbank.knowhow.core_service.service.messaging)
            """)
    public void applicationPackageMethods() {}

    @Around("applicationPackageMethods()")
    public Object logMethodEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        try {
            MDC.put(EXECUTION_CLASS.value(), className);
            MDC.put(EXECUTION_METHOD.value(), methodName);

            if (Objects.isNull(MDC.get(TRACE_ID))) {
                MDC.put(TRACE_ID, UUID.randomUUID().toString());
            }
            if (log.isTraceEnabled()) {
                logMethodStart(joinPoint);
            }

            long startTime = System.currentTimeMillis();
            Object result = null;
            try {
                result = joinPoint.proceed();
                return result;
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                if (log.isTraceEnabled()) {
                    logMethodEndStructured(result, duration);
                }
                if (duration > LOWER_TIME_LIMIT_FOR_SLOW_METHOD) {
                    logSlowMethodStructured(duration, className, methodName, joinPoint.getArgs());
                }
            }
        } finally {
            MDC.clear();
        }
    }

    private void logMethodStart(ProceedingJoinPoint joinPoint) {
        MDC.put(ARGS.value(), truncateArgs(joinPoint.getArgs()));
        log.trace("Method execution started");
    }

    private void logMethodEndStructured(Object result, long duration) {
        MDC.put(EXECUTION_TIME_MS.value(), String.valueOf(duration));
        MDC.put(METHOD_RESULT.value(), truncateResult(result));
        log.trace("Method execution finished");
    }

    private void logSlowMethodStructured(long duration, String className, String methodName, Object[] args) {
        MDC.put(METHOD_DURATION_MS.value(), String.valueOf(duration));
        MDC.put(SLOW_METHOD_CLASS.value(), className);
        MDC.put(SLOW_METHOD_NAME.value(), methodName);
        MDC.put(ARGS.value(), truncateArgs(args));
        log.info("Slow method detected");
    }

    private String truncateArgs(Object[] args) {
        String params = Arrays.stream(args)
                .map(arg -> Objects.nonNull(arg) ? arg.toString() : "null")
                .limit(10)
                .collect(Collectors.joining(PARAM_DELIMITER));
        return params.length() > RESULT_LENGHT_LIMIT
                ? params.substring(0, RESULT_LENGHT_LIMIT) + "..."
                : params;
    }

    private String truncateResult(Object result) {
        if (result == null) return "null";
        String resultStr = result.toString();
        return resultStr.length() > RESULT_LENGHT_LIMIT
                ? resultStr.substring(0, RESULT_LENGHT_LIMIT) + "... [cropped]"
                : resultStr;
    }
}
