package ru.tbank.knowhow.ecxeption;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ProblemDetail catchEntityExistsException(EntityExistsException e) {
        return handleException(e, HttpStatus.CONFLICT, "ENTITY_ALREADY_EXISTS");
    }

    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ProblemDetail catchNotFoundException(Exception e) {
        return handleException(e, HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND");
    }

    @ExceptionHandler
    public ProblemDetail catchAccessDeniedException(AccessDeniedException e) {
        return handleException(e, HttpStatus.FORBIDDEN, "ACCESS_DENIED");
    }

    @ExceptionHandler
    public ProblemDetail catchIllegalArgumentException(IllegalArgumentException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT");
    }

    @ExceptionHandler
    public ProblemDetail catchNullPointerException(NullPointerException e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, "NULL_ARGUMENT");
    }

    @ExceptionHandler
    public ProblemDetail catchMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "METHOD_ARGUMENT_TYPE_MISMATCH");
    }

    @ExceptionHandler
    public ProblemDetail catchConstraintViolationException(ConstraintViolationException e) {
        return handleException(e, HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION");
    }

    @ExceptionHandler
    public ProblemDetail catchCustomException(KnowHowException e) {
        return handleException(e, e.responseStatus(), e.errorCode());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail catchIllegalStateException(IllegalStateException e) {
        return handleException(e, HttpStatus.INTERNAL_SERVER_ERROR, "ILLEGAL_STATE");
    }

    private ProblemDetail handleException(Exception e, HttpStatus status, String errorCode) {
        try {
            MDC.put(PropertyName.EXCEPTION_TYPE.value(), e.getClass().getSimpleName());
            MDC.put(PropertyName.DETAILS.value(), e.getMessage());
            MDC.put(PropertyName.ERROR_CODE.value(), errorCode);
            MDC.put(PropertyName.STATUS.value(), String.valueOf(status.value()));

            log.error("Exception handled: {} - {}", errorCode, e.getMessage(), e);

            var problemDetail = ProblemDetail.forStatusAndDetail(status, status.getReasonPhrase());
            problemDetail.setProperty(PropertyName.TIMESTAMP.value(), Instant.now());
            problemDetail.setProperty(PropertyName.ERROR_CODE.value(), errorCode);
            problemDetail.setProperty(PropertyName.EXCEPTION_TYPE.value(), e.getClass().getSimpleName());
            return problemDetail;
        } finally {
            MDC.clear();
        }
    }
}
