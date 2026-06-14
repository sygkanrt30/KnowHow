package ru.tbank.knowhow.notification_service.service.notification.moderation;

import ru.tbank.shared.events.NotificationContactType;

public interface ModerationService {

    void notifyModeratorCourseAddOnModeration(String contact, String title,
                                              String username, NotificationContactType contactType);

    void notifyUserResultOfModeration(String contact, String reviewResult,
                                      String username, NotificationContactType contactType);
}
