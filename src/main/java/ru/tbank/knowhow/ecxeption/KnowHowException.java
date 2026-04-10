package ru.tbank.knowhow.ecxeption;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
public class KnowHowException extends RuntimeException {

    private final HttpStatus responseStatus;

    protected KnowHowException(String message, Throwable cause, HttpStatus responseStatus) {
        super(message, cause);
        this.responseStatus = responseStatus;
    }

    protected KnowHowException(String message, HttpStatus responseStatus) {
        super(message);
        this.responseStatus = responseStatus;
    }
}
