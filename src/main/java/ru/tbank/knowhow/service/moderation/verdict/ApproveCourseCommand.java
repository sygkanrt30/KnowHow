package ru.tbank.knowhow.service.moderation.verdict;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.courses.CourseStatus;
import ru.tbank.knowhow.model.moderation.ModerationReview;
import ru.tbank.knowhow.model.users.User;


@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ApproveCourseCommand implements VerdictCommand {

    private static final String APPROVED = "approved";

    private final Long courseId;
    private final String moderatorUsername;

    @Override
    public Long getCourseId() {
        return courseId;
    }

    @Override
    public String getModeratorUsername() {
        return moderatorUsername;
    }

    @Override
    public ModerationReview createReview(User moderator, Course course) {
        return ModerationReview.builder()
                .moderator(moderator)
                .course(course)
                .approved(true)
                .build();
    }

    @Override
    public void updateCourseStatusAndModeratorSetNull(Course course) {
        course.setStatus(CourseStatus.PASSED_MODERATION);
        course.setModerator(null);
    }

    @Override
    public String getActionName() {
        return APPROVED;
    }
}
