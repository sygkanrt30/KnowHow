package ru.tbank.knowhow.core_service.service.verification;

import ru.tbank.shared.events.NotificationContactType;

public interface VerificationService {

    void generateAndSendCode(NotificationContactType notificationContactType, Long userId);

    boolean verifyContact(NotificationContactType notificationContactType, String code, Long userId);
}
