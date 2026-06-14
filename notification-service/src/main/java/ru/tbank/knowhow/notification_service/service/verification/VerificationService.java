package ru.tbank.knowhow.notification_service.service.verification;

public interface VerificationService {

    void sendVerificationCodeOnEmail(String email);

    void sendVerificationCodeOnTg(String tg);
}
