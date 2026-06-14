package ru.tbank.knowhow.notification_service.service.notification.moderation;

public interface ModerationService {

    void notifyModeratorCourseAddOnModeration(String contact, String title, String username);

    void notifyUserResultOfModeration(String contact, String reviewResult, String username);
}
