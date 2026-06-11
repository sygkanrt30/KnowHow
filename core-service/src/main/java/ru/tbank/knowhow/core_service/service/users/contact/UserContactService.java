package ru.tbank.knowhow.core_service.service.users.contact;

import ru.tbank.shared.events.NotificationContactType;

public interface UserContactService {

    void insertTgUsername(String tgUsername, Long userId);

    void updateEmail(String email, Long userId);

    String getTgUsername(Long userId);

    NotificationContactType getPrimaryNotificationContact(Long userId);

    NotificationContactType changePrimaryNotificationContact(Long userId, NotificationContactType notificationContactType);

    void verifyContact(Long userId, NotificationContactType notificationContactType);
}
