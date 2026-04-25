package ru.tbank.knowhow.service.moder;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.ModerationReview;
import ru.tbank.knowhow.model.ModeratorLoad;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.ModerationReviewRepository;
import ru.tbank.knowhow.repository.ModeratorLoadRepository;
import ru.tbank.knowhow.repository.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModerationServiceImpl implements ModerationService {

    private final ModeratorLoadRepository moderatorLoadRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModerationReviewRepository moderationReviewRepository;

    @Override
    public User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());
        log.debug("Assigned moderator {} to new course", moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }

    @Override
    @Transactional
    public void approveCourse(Long courseId, String moderatorUsername) {
        User moderator = userRepository.findByUsername(moderatorUsername)
                .orElseThrow(() -> new EntityNotFoundException("Модератор не найден"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс не найден"));

        if (moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId())) {
            log.warn("Moderator {} already voted on course {}", moderator.getId(), courseId);
            throw new RuntimeException("Вы уже проголосовали за этот курс");
        }

        ModerationReview review = ModerationReview.builder()
                .moderator(moderator)
                .course(course)
                .approved(true)
                .build();
        moderationReviewRepository.save(review);

        moderatorLoadRepository.decrementCoursesInModeration(moderator.getId());

        course.setStatus(CourseStatus.PASSED_MODERATION);
        courseRepository.save(course);

        log.debug("Course {} approved by moderator {}", courseId, moderator.getId());
    }
}