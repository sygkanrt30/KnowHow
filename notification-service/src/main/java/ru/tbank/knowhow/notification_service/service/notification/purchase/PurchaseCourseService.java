package ru.tbank.knowhow.notification_service.service.notification.purchase;

public interface PurchaseCourseService {

    void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse, String authorName);
}
