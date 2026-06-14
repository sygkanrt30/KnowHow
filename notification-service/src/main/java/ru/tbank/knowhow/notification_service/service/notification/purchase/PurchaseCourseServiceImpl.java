package ru.tbank.knowhow.notification_service.service.notification.purchase;

import org.springframework.stereotype.Service;
import ru.tbank.shared.events.NotificationContactType;

@Service
public class PurchaseCourseServiceImpl implements PurchaseCourseService {

    @Override
    public void notifyAuthorCoursePurchase(String contact, int numberOfPurchasedCourse,
                                           String authorName, NotificationContactType contactType) {

    }
}
