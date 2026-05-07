package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class RegistrationException extends KnowHowException {

    public RegistrationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.CONFLICT);
    }
}
