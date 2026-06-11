package ru.tbank.knowhow.notification_service.event.rabbitmq.service.notification.purchase;

public interface PurchaseCourseService {

    void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse, String authorName);
}
