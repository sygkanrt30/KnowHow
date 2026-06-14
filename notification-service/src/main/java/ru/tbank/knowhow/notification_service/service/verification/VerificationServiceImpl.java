package ru.tbank.knowhow.notification_service.service.verification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.notification_service.service.email.Sender;
import ru.tbank.knowhow.notification_service.service.email.EmailSamples;

@Service
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private static final String SUBJECT = "Email Verification";
    private static final String CODE_PLACEHOLDER = "{{code}}";

    private final Sender emailSender;
    private final EmailSamples emailSamples;

    public VerificationServiceImpl(Sender emailSender) {
        this.emailSender = emailSender;
        this.emailSamples = new EmailSamples();
    }

    @Override
    public void sendVerificationCodeOnEmail(String email, String code) {
        String sample = emailSamples.getVerificationSample();
        String body = sample.replace(CODE_PLACEHOLDER, code);
        emailSender.send(email, body, null, SUBJECT);
        log.info("Email verification code sent to {}", email);
    }
}
