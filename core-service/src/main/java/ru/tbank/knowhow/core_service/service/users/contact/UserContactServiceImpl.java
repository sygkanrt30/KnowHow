package ru.tbank.knowhow.core_service.service.users.contact;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.core_service.model.users.UserContact;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserContactServiceImpl implements UserContactService {

    private final GetUserService getUserService;

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

    private UserContact getUserContact(Long userId) {
        return getUserService.getByIdOrElseThrow(userId).getUserContact();
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId) {
        UserContact userContact = getUserContact(userId);
        userContact.setEmailVerified(true);
        log.debug("Email: {} has been verified", userContact.getEmail());
    }
}
