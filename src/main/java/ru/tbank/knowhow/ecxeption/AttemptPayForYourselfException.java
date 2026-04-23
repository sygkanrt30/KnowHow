package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class AttemptPayForYourselfException extends KnowHowException {

    public AttemptPayForYourselfException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
