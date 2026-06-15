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

    public String getCoursePurchaseSample() {
        return reader.readSample("/course_purchase_email.html");
    }

    public String getAddToModerationSample() {
        return reader.readSample("/add_2_moderation_email.html");
    }

    public String getModerationResultSample() {
        return reader.readSample("/result_moderation_email.html");
    }

    public String getApproveSample() {
        return reader.readSample("/review_results/approve.html");
    }

    public String getRejectSample() {
        return reader.readSample("/review_results/reject.html");
    }
}
