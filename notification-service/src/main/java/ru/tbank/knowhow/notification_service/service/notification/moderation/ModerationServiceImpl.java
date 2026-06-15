package ru.tbank.knowhow.notification_service.service.notification.moderation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.notification_service.service.email.EmailSamples;
import ru.tbank.knowhow.notification_service.service.email.EmailSender;
import ru.tbank.shared.events.ReviewResult;

@Slf4j
@Service
public class ModerationServiceImpl implements ModerationService {

    private static final String USERNAME_PLACEHOLDER = "{{username}}";
    private static final String TITLE_PLACEHOLDER = "{{title}}";
    private static final String REVIEW_RESULT_PLACEHOLDER = "{{reviewResult}}";

    private final EmailSender emailSender;
    private final EmailSamples emailSamples;
    private final String addToModerationSubject;
    private final String reviewResultSubject;

    public ModerationServiceImpl(EmailSender emailSender,
                                 @Value("${email.subject.add_to_moderation}") String addToModerationSubject,
                                 @Value("${email.subject.review_result}") String reviewResultSubject) {
        this.emailSender = emailSender;
        this.emailSamples = new EmailSamples();
        this.addToModerationSubject = addToModerationSubject;
        this.reviewResultSubject = reviewResultSubject;
    }

    @Override
    public void notifyModeratorCourseAddOnModeration(String contact, String title, String username) {
        String sample = emailSamples.getAddToModerationSample();
        String body = sample.replace(USERNAME_PLACEHOLDER, username).replace(TITLE_PLACEHOLDER, title);
        emailSender.send(contact, body, addToModerationSubject);
        log.info("Alert to moderator about course add on moderation sent to {}", contact);
    }

    @Override
    public void notifyUserResultOfModeration(String contact, ReviewResult reviewResult, String username) {
        String sample = emailSamples.getModerationResultSample();
        String action = switch (reviewResult) {
            case APPROVE -> emailSamples.getApproveSample();
            case REJECT -> emailSamples.getRejectSample();
        };
        String body = sample.replace(USERNAME_PLACEHOLDER, username).replace(REVIEW_RESULT_PLACEHOLDER, action);
        emailSender.send(contact, body, reviewResultSubject);
        log.info("Alert to user about result of moderation sent to {}", contact);
    }
}
