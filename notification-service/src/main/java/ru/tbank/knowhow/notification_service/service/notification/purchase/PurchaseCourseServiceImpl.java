package ru.tbank.knowhow.notification_service.service.notification.purchase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.notification_service.service.email.EmailSamples;
import ru.tbank.knowhow.notification_service.service.email.Sender;

@Service
@Slf4j
public class PurchaseCourseServiceImpl implements PurchaseCourseService {

    private static final String USERNAME_PLACEHOLDER = "{{username}}";
    private static final String NUMBER_OF_PURCHASES_PLACEHOLDER = "{{numberOfPurchasedCourse}}";

    private final Sender emailSender;
    private final EmailSamples emailSamples;
    private final String subject;

    public PurchaseCourseServiceImpl(Sender emailSender, @Value("${email.subject.purchase}") String subject) {
        this.emailSender = emailSender;
        this.emailSamples = new EmailSamples();
        this.subject = subject;

    }

    @Override
    public void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse, String authorName) {
        String sample = emailSamples.getCoursePurchaseSample();
        String body = sample.replace(USERNAME_PLACEHOLDER, authorName)
                .replace(NUMBER_OF_PURCHASES_PLACEHOLDER, Integer.toString(numberOfPurchasedCourse));
        emailSender.send(contact, body, subject);
        log.info("Alert to author about course purchasing sent to {}", contact);
    }
}
