package ru.tbank.knowhow.core_service.service.verification.attempt;

public interface AttemptLimiter {

    boolean isAttemptAllowed(String contact);

    void registerFailedAttempt(String contact);

    void resetAttempts(String contact);

    int getAttemptCount(String contact);
}
