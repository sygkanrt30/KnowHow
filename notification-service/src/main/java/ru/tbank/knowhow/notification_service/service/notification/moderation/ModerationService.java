package ru.tbank.knowhow.notification_service.service.notification.moderation;

import ru.tbank.shared.events.ReviewResult;

public interface ModerationService {

    void notifyModeratorCourseAddOnModeration(String contact, String title, String username);

    void notifyUserResultOfModeration(String contact, ReviewResult reviewResult, String username);
}
