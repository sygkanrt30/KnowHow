package ru.tbank.knowhow.notification_service.event.rabbitmq.service.verification;

import org.springframework.stereotype.Service;

@Service
public class VerificationServiceImpl implements VerificationService {
    @Override
    public void sendVerificationCodeOnEmail(String email) {

    }

    @Override
    public void sendVerificationCodeOnTg(String tg) {

    }
}
