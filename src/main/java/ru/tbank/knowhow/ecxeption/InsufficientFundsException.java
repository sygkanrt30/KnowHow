package ru.tbank.knowhow.ecxeption;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends KnowHowException {

    public InsufficientFundsException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED);
    }
}
