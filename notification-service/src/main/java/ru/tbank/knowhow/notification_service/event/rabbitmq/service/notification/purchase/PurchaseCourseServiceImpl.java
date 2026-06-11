package ru.tbank.knowhow.notification_service.event.rabbitmq.service.notification.purchase;

import org.springframework.stereotype.Service;
import ru.tbank.shared.events.notification.CoursePurchaseNotificationEvent;

@Service
public class PurchaseCourseServiceImpl implements PurchaseCourseService {

    @Override
    public void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse, String authorName) {

    }
}
