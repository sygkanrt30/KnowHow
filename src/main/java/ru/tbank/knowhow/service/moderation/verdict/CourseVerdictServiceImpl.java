package ru.tbank.knowhow.service.moderation.verdict;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.moderation.ModerationReview;
import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.repository.moderation.ModerationReviewRepository;
import ru.tbank.knowhow.repository.moderation.ModeratorLoadRepository;
import ru.tbank.knowhow.service.courses.GetCourseService;
import ru.tbank.knowhow.service.users.GetUserService;

@RequiredArgsConstructor
@Service
@Slf4j
public class CourseVerdictServiceImpl implements CourseVerdictService {

    private final GetUserService getUserService;
    private final GetCourseService getCourseService;
    private final ModerationReviewRepository moderationReviewRepository;
    private final ModeratorLoadRepository moderatorLoadRepository;

    @Override
    @Transactional
    public void approveCourse(Long courseId, String moderatorUsername) {
        executeCommand(new ApproveCourseCommand(courseId, moderatorUsername));
    }

    @Override
    @Transactional
    public void rejectCourse(Long courseId, String moderatorUsername, String rejectionReason) {
        executeCommand(new RejectCourseCommand(courseId, moderatorUsername, rejectionReason));
    }

    private void executeCommand(VerdictCommand command) {
        User moderator = getUserService.getByUsernameOrElseThrow(command.getModeratorUsername());
        Course course = getCourseService.getCourseByIdOrElseThrow(command.getCourseId());

        validateNotAlreadyProcessed(course.getId(), moderator.getId());

        ModerationReview review = command.createReview(moderator, course);
        moderationReviewRepository.save(review);

        moderatorLoadRepository.decrementCoursesInModeration(moderator.getId());
        command.updateCourseStatusAndModeratorSetNull(course);
        log.debug("Course {} {} by moderator {}", course.getId(), command.getActionName(), moderator.getId());
    }

    private void validateNotAlreadyProcessed(Long courseId, Long moderatorId) {
        if (moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderatorId)) {
            throw new EntityExistsException("Moderator has already processed this course");
        }
    }
}
