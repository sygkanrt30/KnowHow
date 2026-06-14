package ru.tbank.knowhow.core_service.util;

import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.notification.AddCourseForModerationNotificationEvent;
import ru.tbank.shared.events.notification.CoursePurchaseNotificationEvent;
import ru.tbank.shared.events.notification.ResultReviewNotificationEvent;

public final class NotificationEventFabric {

    public Event createResultReviewEvent(String contact, String reviewResult, String username) {
        return new ResultReviewNotificationEvent(contact, reviewResult, username);
    }

    public Event createAddCourseForModerationEvent(String contact, String title, String username) {
        return new AddCourseForModerationNotificationEvent(contact, title, username);
    }

    public Event createCoursePurchaseEvent(String contact, int numberOfPurchasedCourse, String authorName) {
        return new CoursePurchaseNotificationEvent(contact, numberOfPurchasedCourse, authorName);
    }
}
