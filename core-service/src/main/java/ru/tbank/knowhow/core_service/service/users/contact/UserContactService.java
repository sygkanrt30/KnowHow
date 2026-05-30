package ru.tbank.knowhow.core_service.service.users.contact;

import ru.tbank.knowhow.core_service.model.users.NotificationContact;

public interface UserContactService {

    void insertTgUsername(String tgUsername, Long userId);

    void updateEmail(String email, Long userId);

    String getTgUsername(Long userId);

    NotificationContact getPrimaryNotificationContact(Long userId);

    NotificationContact changePrimaryNotificationContact(Long userId, NotificationContact notificationContact);
}
