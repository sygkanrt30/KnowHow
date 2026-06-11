package ru.tbank.knowhow.notification_service.event.rabbitmq.service.notification.moderation;

import org.springframework.stereotype.Service;
import ru.tbank.shared.events.notification.AddCourseForModerationNotificationEvent;
import ru.tbank.shared.events.notification.ResultReviewNotificationEvent;

@Service
public class ModerationServiceImpl implements ModerationService {

    @Override
    public void notifyModeratorCourseAddOnModeration(String contact, String title, String username) {

    }

    @Override
    public void notifyUserResultOfModeration(String contact, String reviewResult, String username) {

    }
}
