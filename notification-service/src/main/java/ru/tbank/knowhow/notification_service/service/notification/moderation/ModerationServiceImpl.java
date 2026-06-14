package ru.tbank.knowhow.notification_service.service.notification.moderation;

import org.springframework.stereotype.Service;
import ru.tbank.shared.events.NotificationContactType;

@Service
public class ModerationServiceImpl implements ModerationService {

    @Override
    public void notifyModeratorCourseAddOnModeration(String contact, String title,
                                                     String username, NotificationContactType contactType) {

    }

    @Override
    public void notifyUserResultOfModeration(String contact, String reviewResult,
                                             String username, NotificationContactType contactType) {

    }
}
