package ru.tbank.knowhow.notification_service.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailSender implements Sender {

    private static final String ENCODING = "UTF-8";

    private final JavaMailSender mailSender;
    private final String from;

    public EmailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void send(String contact, String body, String subject) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, ENCODING);
            helper.setTo(contact);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom(from);

            mailSender.send(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.debug("Sent email to {} with subject {}", contact, subject);
    }
}