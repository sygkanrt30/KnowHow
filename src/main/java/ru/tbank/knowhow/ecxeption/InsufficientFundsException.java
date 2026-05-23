package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends KnowHowException {

    private static final String ERROR_CODE = "INSUFFICIENT_FUNDS";

    public InsufficientFundsException(String message) {
        super(message, ERROR_CODE, HttpStatus.PAYMENT_REQUIRED);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.PAYMENT_REQUIRED, cause);
    }
}