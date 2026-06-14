package ru.tbank.knowhow.notification_service.service.notification.purchase;

import ru.tbank.shared.events.NotificationContactType;

public interface PurchaseCourseService {

    void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse,
                                    String authorName, NotificationContactType contactType);
}
