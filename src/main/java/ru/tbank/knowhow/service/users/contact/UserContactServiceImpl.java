package ru.tbank.knowhow.service.users.contact;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.users.NotificationContact;
import ru.tbank.knowhow.model.users.UserContact;
import ru.tbank.knowhow.service.users.GetUserService;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContactServiceImpl implements UserContactService {

    private final GetUserService getUserService;

    @Override
    @Transactional
    public void insertTgUsername(String tgUsername, Long userId) {
        UserContact userContact = getUserContact(userId);
        String currentTgUsername = userContact.getTgUsername();
        if (Objects.nonNull(currentTgUsername) && currentTgUsername.equals(tgUsername)) {
            log.debug("Current and new Tg username same");
            return;
        }
        userContact.setTgUsername(tgUsername);
    }

    private UserContact getUserContact(Long userId) {
        return getUserService.getByIdOrElseThrow(userId).getUserContact();
    }

    @Override
    @Transactional
    public void updateEmail(String email, Long userId) {
        UserContact userContact = getUserContact(userId);
        String currentEmail = userContact.getEmail();
        if (Objects.nonNull(currentEmail) && currentEmail.equals(email)) {
            log.debug("Current and New Email same");
            return;
        }
        userContact.setEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public String getTgUsername(Long userId) {
        UserContact userContact = getUserContact(userId);
        String currentTgUsername = userContact.getTgUsername();
        if (Objects.isNull(currentTgUsername)) {
            throw new EmptyResultDataAccessException("Tg username is null", 1);
        }
        return currentTgUsername;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationContact getPrimaryNotificationContact(Long userId) {
        return getUserContact(userId).getPrimaryNotificationContact();
    }

    @Override
    @Transactional
    public NotificationContact changePrimaryNotificationContact(Long userId, NotificationContact notificationContact) {
        UserContact userContact = getUserContact(userId);
        NotificationContact currentNotificationContact = userContact.getPrimaryNotificationContact();

        if (Objects.nonNull(currentNotificationContact) && currentNotificationContact.equals(notificationContact)) {
            log.debug("Current primary notification contact is equal to new notification contact");
            return currentNotificationContact;
        }
        userContact.setPrimaryNotificationContact(notificationContact);
        log.info("Primary notification contact has been changed to {}", notificationContact);
        return notificationContact;
    }
}
