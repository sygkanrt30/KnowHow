package ru.tbank.knowhow.core_service.service.verification;

import ru.tbank.knowhow.core_service.model.users.NotificationContact;

public interface VerificationService {

    void generateAndSendCode(NotificationContact notificationContact, Long userId);

    boolean verifyContact(NotificationContact notificationContact, String code, Long userId);
}
