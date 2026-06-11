package ru.tbank.knowhow.notification_service.event.rabbitmq.service.verification;

public interface VerificationService {

    void sendVerificationCodeOnEmail(String email);

    void sendVerificationCodeOnTg(String tg);
}
