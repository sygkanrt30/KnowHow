package ru.tbank.knowhow.ecxeption;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
public class KnowHowException extends RuntimeException {

    private final HttpStatus responseStatus;
    private final String errorCode;

    protected KnowHowException(String message, String errorCode, HttpStatus responseStatus, Throwable cause) {
        super(message, cause);
        this.responseStatus = responseStatus;
        this.errorCode = errorCode;
    }

    protected KnowHowException(String message, String errorCode, HttpStatus responseStatus) {
        this(message, errorCode, responseStatus, null);
    }
}