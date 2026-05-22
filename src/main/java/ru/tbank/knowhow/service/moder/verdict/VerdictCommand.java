package ru.tbank.knowhow.service.moder.verdict;

import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.ModerationReview;
import ru.tbank.knowhow.model.User;

interface VerdictCommand {

    Long getCourseId();

    String getModeratorUsername();

    ModerationReview createReview(User moderator, Course course);

    void updateCourseStatusAndModeratorSetNull(Course course);

    String getActionName();
}
