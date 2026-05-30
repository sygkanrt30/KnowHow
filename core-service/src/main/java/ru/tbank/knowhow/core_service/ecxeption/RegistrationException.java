package ru.tbank.knowhow.core_service.ecxeption;

import org.springframework.http.HttpStatus;

public class RegistrationException extends KnowHowException {

    private static final String ERROR_CODE = "REGISTRATION_ERROR";

    public RegistrationException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT, cause);
    }
}
