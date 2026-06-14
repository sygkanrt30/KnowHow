package ru.tbank.knowhow.core_service.service.verification;

public interface VerificationService {

    void generateAndSendCode(Long userId);

    boolean verifyContact(String code, Long userId);
}
