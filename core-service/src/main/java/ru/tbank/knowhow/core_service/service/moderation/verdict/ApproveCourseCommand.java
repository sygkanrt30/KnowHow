package ru.tbank.knowhow.core_service.service.moderation.verdict;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.courses.CourseStatus;
import ru.tbank.knowhow.core_service.model.moderation.ModerationReview;
import ru.tbank.knowhow.core_service.model.users.User;


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
