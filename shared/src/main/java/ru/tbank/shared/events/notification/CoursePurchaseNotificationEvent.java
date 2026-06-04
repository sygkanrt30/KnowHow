package ru.tbank.shared.events.notification;

import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;

import java.util.Objects;

public final class CoursePurchaseNotificationEvent extends AbstractEvent {

    private final int numberOfPurchasedCourse;
    private final String authorName;

    public CoursePurchaseNotificationEvent(String contact, int numberOfPurchasedCourse, String authorName) {
        super(EventType.NOTIFICATION, contact);
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
