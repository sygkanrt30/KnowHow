package ru.tbank.knowhow.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class MethodLoggingAspect {

    private static final int LOWER_TIME_LIMIT_FOR_SLOW_METHOD = 1000;
    private static final int RESULT_LENGHT_LIMIT = 500;
    private static final String PARAM_DELIMITER = ", ";

    @Pointcut("""
                    execution(* ru.tbank.knowhow..*.*(..)) &&
                    !within(ru.tbank.knowhow.ecxeption..*) &&
                    !within(ru.tbank.knowhow.config..*)
            """)
    public void applicationPackageMethods() {}

    @Around("applicationPackageMethods()")
    public Object logMethodEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        if (log.isTraceEnabled()) {
            logMethodStart(joinPoint, className, methodName);
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (log.isTraceEnabled()) {
                logMethodEnd(result, className, methodName, duration);
            }
            logIfMethodIsSlow(duration, className, methodName);
        }
    }

    private void logMethodStart(ProceedingJoinPoint joinPoint, String className, String methodName) {
        Object[] args = joinPoint.getArgs();
        String params = Arrays.stream(args)
                .map(arg -> Objects.nonNull(arg) ? arg.toString() : "null")
                .collect(Collectors.joining(PARAM_DELIMITER));
        log.trace("-> Вход в {}.{}({})", className, methodName, params);
    }

    private void logMethodEnd(Object result, String className, String methodName, long duration) {
        String resultStr = Objects.nonNull(result) ? result.toString() : "null";
        if (resultStr.length() > RESULT_LENGHT_LIMIT) {
            resultStr = resultStr.substring(0, RESULT_LENGHT_LIMIT) + "... [обрезано]";
        }
        log.trace("<- Выход из {}.{} -> {} ({} мс)", className, methodName, resultStr, duration);
    }

    private void logIfMethodIsSlow(long duration, String className, String methodName) {
        if (duration > LOWER_TIME_LIMIT_FOR_SLOW_METHOD) {
            log.info("Медленный метод {}.{}: {} мс", className, methodName, duration);
        }
    }
}
