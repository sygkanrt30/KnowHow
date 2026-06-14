package ru.tbank.knowhow.notification_service.service.verification;

import org.springframework.stereotype.Service;

@Service
public class VerificationServiceImpl implements VerificationService {

    @Override
    public void sendVerificationCodeOnEmail(String email, String code) {

    }

    @Override
    public void sendVerificationCodeOnTg(String tg, String code) {

    }
}
