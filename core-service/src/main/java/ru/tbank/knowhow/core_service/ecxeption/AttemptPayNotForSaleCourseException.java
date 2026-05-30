package ru.tbank.knowhow.core_service.ecxeption;

import org.springframework.http.HttpStatus;

public class AttemptPayNotForSaleCourseException extends KnowHowException {

    private static final String ERROR_CODE = "PAY_NOT_FOR_SALE_COURSE";

    public AttemptPayNotForSaleCourseException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public AttemptPayNotForSaleCourseException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, cause);
    }
}
