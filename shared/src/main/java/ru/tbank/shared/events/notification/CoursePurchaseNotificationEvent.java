package ru.tbank.shared.events.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;
import ru.tbank.shared.events.NotificationContactType;

import java.util.Objects;

public final class CoursePurchaseNotificationEvent extends AbstractEvent {

    private final int numberOfPurchasedCourse;
    private final String authorName;

    @JsonCreator
    public CoursePurchaseNotificationEvent(
            @JsonProperty("contact") String contact,
            @JsonProperty("numberOfPurchasedCourse") int numberOfPurchasedCourse,
            @JsonProperty("authorName") String authorName,
            @JsonProperty("contactType") NotificationContactType contactType) {
        super(EventType.NOTIFICATION, contact, contactType);
        validateParams(numberOfPurchasedCourse, authorName);
        this.numberOfPurchasedCourse = numberOfPurchasedCourse;
        this.authorName = authorName;
    }

    private void validateParams(int n, String authorName) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be greater than 0");
        }
        if (Objects.isNull(authorName)) {
            throw new IllegalArgumentException("authorName must not be null");
        }
    }

    public int getNumberOfPurchasedCourse() {
        return numberOfPurchasedCourse;
    }

    public String getAuthorName() {
        return authorName;
    }
}
