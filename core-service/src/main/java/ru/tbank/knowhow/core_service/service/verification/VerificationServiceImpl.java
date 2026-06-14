package ru.tbank.knowhow.core_service.service.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.core_service.ecxeption.VerificationException;
import ru.tbank.knowhow.core_service.model.users.UserContact;
import ru.tbank.knowhow.core_service.service.event.VerificationEventPublisher;
import ru.tbank.knowhow.core_service.service.users.GetUserService;
import ru.tbank.knowhow.core_service.service.users.contact.UserContactService;
import ru.tbank.knowhow.core_service.service.verification.attempt.AttemptLimiter;
import ru.tbank.knowhow.core_service.service.verification.code_storage.CodeStorage;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final GetUserService getUserService;
    private final VerificationEventPublisher eventPublisher;
    private final CodeStorage codeStorage;
    private final AttemptLimiter attemptLimiter;
    private final UserContactService userContactService;

    @Override
    @Transactional(readOnly = true)
    public void generateAndSendCode(Long userId) {
        String contact = getContact(userId);
        if (codeStorage.isBlocked(contact)) {
            throw new VerificationException("To many request!", HttpStatus.TOO_MANY_REQUESTS);
        }
        String code = CodeGenerator.generateCode();
        eventPublisher.createAndPublishVerificationEvent(contact, code);
        saveAndCreateLockForResend(code, contact);
        log.debug("Code sent to contact {} successfully", contact);
    }

    private void saveAndCreateLockForResend(String code, String contact) {
        codeStorage.saveCode(code, contact);
        codeStorage.blockForResend(contact);
    }

    private @NonNull String getContact(Long userId) {
        UserContact userContact = getUserService.getByIdOrElseThrow(userId).getUserContact();
        String email = userContact.getEmail();
        boolean emailVerified = userContact.isEmailVerified();
        validateContact(email, emailVerified);
        return email;
    }

    private void validateContact(String email, boolean emailVerified) {
        if (Objects.isNull(email) && emailVerified) {
            throw new VerificationException("Either email is not specified in DB, or it has already been verified");
        }
    }

    @Override
    @Transactional
    public boolean verifyContact(String code, Long userId) {
        String contact = getContact(userId);
        if (!attemptLimiter.isAttemptAllowed(contact)) {
            throw new VerificationException("Attempt limit reached", HttpStatus.TOO_MANY_REQUESTS);
        }
        Optional<String> storedCode = codeStorage.getCode(contact);
        if (storedCode.isEmpty() || !code.equals(storedCode.get())) {
            attemptLimiter.registerFailedAttempt(contact);
            return false;
        }
        userContactService.verifyEmail(userId);
        resetStorageForThisContact(contact);
        log.info("Email verification successful");
        return true;
    }

    private void resetStorageForThisContact(String contact) {
        codeStorage.deleteCode(contact);
        attemptLimiter.resetAttempts(contact);
    }
}