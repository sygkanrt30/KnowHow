package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class AttemptPayNotForSaleCourseException extends KnowHowException {

    public AttemptPayNotForSaleCourseException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
