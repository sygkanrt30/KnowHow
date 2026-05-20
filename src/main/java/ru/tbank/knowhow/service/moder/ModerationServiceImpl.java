package ru.tbank.knowhow.service.moder;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.model.*;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.ModerationReviewRepository;
import ru.tbank.knowhow.repository.ModeratorLoadRepository;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModerationServiceImpl implements ModerationService {

    private final ModeratorLoadRepository  moderatorLoadRepository;
    private final GetUserService getUserService;
    private final CourseRepository courseRepository;
    private final ModerationReviewRepository moderationReviewRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }

    @Override
    @Transactional
    public void approveCourse(Long courseId, String moderatorUsername) {
        User moderator = getUserService.findByUsername(moderatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Moderator not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс не найден"));

        if (moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId())) {
            throw new EntityExistsException("Модератор уже обработал этот курс");
        }

        ModerationReview review = ModerationReview.builder()
                .moderator(moderator)
                .course(course)
                .approved(true)
                .build();
        moderationReviewRepository.save(review);

        moderatorLoadRepository.decrementCoursesInModeration(moderator.getId());

        course.setStatus(CourseStatus.PASSED_MODERATION);
        course.setModerator(null);

        log.debug("Course {} approved by moderator {}", courseId, moderator.getId());
    }

    @Override
    @Transactional
    public void rejectCourse(Long courseId, String moderatorUsername, String rejectionReason) {
        User moderator = getUserService.findByUsername(moderatorUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Moderator not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс не найден"));

        if (moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId())) {
            throw new EntityExistsException("Модератор уже обработал этот курс");
        }

        ModerationReview review = ModerationReview.builder()
                .moderator(moderator)
                .course(course)
                .approved(false)
                .rejectionReason(rejectionReason)
                .build();
        moderationReviewRepository.save(review);

        moderatorLoadRepository.decrementCoursesInModeration(moderator.getId());

        course.setStatus(CourseStatus.NOT_ACCEPTED);
        course.setModerator(null);
    }

    @Override
    public List<CourseDto> findAllCoursesOnModerationByModeratorId(Long moderationId) {
        User moderator = getUserService.findById(moderationId)
                .orElseThrow(() -> new EntityNotFoundException("Moderator not found"));

        List<Course> courses =  courseRepository.findAllByModerator(moderator);

        return courses.stream()
                .filter(course -> course.getStatus().equals(CourseStatus.ON_MODERATION))
                .map(courseMapper::toDto)
                .toList();
    }
}
