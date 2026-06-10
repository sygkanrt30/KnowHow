package ru.tbank.knowhow.core_service.ecxeption;

import org.springframework.http.HttpStatus;

public class VerificationException extends KnowHowException{

    private static final String ERROR_CODE = "VERIFICATION_ERROR";

    public VerificationException(String message) {
        super(message, ERROR_CODE, HttpStatus.NOT_ACCEPTABLE);
    }

    public VerificationException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.NOT_ACCEPTABLE, cause);
    }

    public VerificationException(String message, HttpStatus httpStatus) {
        super(message, ERROR_CODE, httpStatus);
    }
}
