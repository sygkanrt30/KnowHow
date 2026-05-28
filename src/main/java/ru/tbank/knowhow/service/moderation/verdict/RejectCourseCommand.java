package ru.tbank.knowhow.service.moderation.verdict;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.courses.CourseStatus;
import ru.tbank.knowhow.model.moderation.ModerationReview;
import ru.tbank.knowhow.model.users.User;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@Slf4j
class RejectCourseCommand implements VerdictCommand {

    private static final String REJECTED = "rejected";

    private final Long courseId;
    private final String moderatorUsername;
    private final String rejectionReason;

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
        log.debug("Rejection reason for course with id({}): {}", courseId, rejectionReason);
        return ModerationReview.builder()
                .moderator(moderator)
                .course(course)
                .approved(false)
                .rejectionReason(rejectionReason)
                .build();
    }

    @Override
    public void updateCourseStatusAndModeratorSetNull(Course course) {
        course.setStatus(CourseStatus.NOT_ACCEPTED);
        course.setModerator(null);
    }

    @Override
    public String getActionName() {
        return REJECTED;
    }
}
