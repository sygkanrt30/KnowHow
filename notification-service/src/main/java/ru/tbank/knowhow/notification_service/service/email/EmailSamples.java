package ru.tbank.knowhow.notification_service.service.email;

import ru.tbank.knowhow.notification_service.util.SampleReader;

public class EmailSamples {

    private final SampleReader reader;

    public EmailSamples() {
        reader = new SampleReader();
    }

    public String getVerificationSample() {
        return reader.readSample("/verification_email.html");
    }
}
