package ru.tbank.knowhow.core_service.util;

import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.NotificationContactType;
import ru.tbank.shared.events.notification.AddCourseForModerationNotificationEvent;
import ru.tbank.shared.events.notification.CoursePurchaseNotificationEvent;
import ru.tbank.shared.events.notification.ResultReviewNotificationEvent;

public final class NotificationEventFabric {

    private static final char AT = '@';

    public Event createResultReviewEvent(String contact, String reviewResult, String username) {
        NotificationContactType contactType = recognizeNotificationType(contact);
        return new ResultReviewNotificationEvent(contact, reviewResult, username, contactType);
    }

    public Event createAddCourseForModerationEvent(String contact, String title, String username) {
        NotificationContactType contactType = recognizeNotificationType(contact);
        return new AddCourseForModerationNotificationEvent(contact, title, username, contactType);
    }

    public Event createCoursePurchaseEvent(String contact, int numberOfPurchasedCourse, String authorName) {
        NotificationContactType contactType = recognizeNotificationType(contact);
        return new CoursePurchaseNotificationEvent(contact, numberOfPurchasedCourse, authorName, contactType);
    }

    private NotificationContactType recognizeNotificationType(String contact) {
        for (int i = 0; i < contact.length(); i++) {
            if (contact.charAt(i) == AT) {
                return NotificationContactType.EMAIL;
            }
        }
        return NotificationContactType.TELEGRAM;
    }
}
