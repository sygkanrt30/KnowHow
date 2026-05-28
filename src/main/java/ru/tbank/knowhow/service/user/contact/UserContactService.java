package ru.tbank.knowhow.service.user.contact;

import ru.tbank.knowhow.model.NotificationContact;

public interface UserContactService {

    void insertTgUsername(String tgUsername, Long userId);

    void updateEmail(String email, Long userId);

    String getTgUsername(Long userId);

    NotificationContact getPrimaryNotificationContact(Long userId);

    NotificationContact changePrimaryNotificationContact(Long userId, NotificationContact notificationContact);
}
