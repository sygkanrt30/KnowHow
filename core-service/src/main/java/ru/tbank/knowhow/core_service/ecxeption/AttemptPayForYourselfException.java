package ru.tbank.knowhow.core_service.ecxeption;

import org.springframework.http.HttpStatus;

public class AttemptPayForYourselfException extends KnowHowException {

    private static final String ERROR_CODE = "PAY_FOR_YOURSELF";

    public AttemptPayForYourselfException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public AttemptPayForYourselfException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, cause);
    }
}
