package ru.tbank.knowhow.core_service.service.moderation.verdict;

import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.moderation.ModerationReview;
import ru.tbank.knowhow.core_service.model.users.User;

interface VerdictCommand {

    Long getCourseId();

    String getModeratorUsername();

    ModerationReview createReview(User moderator, Course course);

    void updateCourseStatusAndModeratorSetNull(Course course);

    String getActionName();
}
