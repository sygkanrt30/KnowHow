package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class LoginException extends KnowHowException {

    private static final String ERROR_CODE = "AUTHENTICATION_ERROR";

    public LoginException(String message) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    public LoginException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.UNAUTHORIZED, cause);
    }
}
