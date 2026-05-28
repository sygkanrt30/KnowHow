package ru.tbank.knowhow.service.moderation.verdict;

import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.moderation.ModerationReview;
import ru.tbank.knowhow.model.users.User;

interface VerdictCommand {

    Long getCourseId();

    String getModeratorUsername();

    ModerationReview createReview(User moderator, Course course);

    void updateCourseStatusAndModeratorSetNull(Course course);

    String getActionName();
}
