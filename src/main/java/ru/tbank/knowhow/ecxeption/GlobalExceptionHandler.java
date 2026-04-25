package ru.tbank.knowhow.ecxeption;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ProblemDetail catchEntityExistsException(EntityExistsException e) {
        return getAppErrorHandlerResponseDto(e, HttpStatus.CONFLICT);
    }

    @ExceptionHandler({EntityNotFoundException.class, UsernameNotFoundException.class})
    public ProblemDetail catchNotFoundException(Exception e) {
        return getAppErrorHandlerResponseDto(e, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler()
    public ProblemDetail catchAccessDeniedException(AccessDeniedException e) {
        return getAppErrorHandlerResponseDto(e, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler
    public ProblemDetail catchIllegalArgumentException(IllegalArgumentException e) {
        return getAppErrorHandlerResponseDto(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ProblemDetail catchCustomException(KnowHowException e) {
        return getAppErrorHandlerResponseDto(e, e.responseStatus());
    }

    private ProblemDetail getAppErrorHandlerResponseDto(Exception e, HttpStatus status) {
        String error = e.getMessage();
        var problemDetail = ProblemDetail.forStatusAndDetail(status, status.getReasonPhrase());
        problemDetail.setProperty(PropertyName.TIMESTAMP.value(), Instant.now());
        log.error(error, e.getCause());
        return problemDetail;
    }
}
