package ru.tbank.knowhow.service.course.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseBusinessDetails;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.dto.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.course.UpdateCourseService;
import ru.tbank.knowhow.service.moder.ModeratorManager;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateCourseServiceImpl implements UpdateCourseService {

    private final CourseMapper courseMapper;
    private final ModeratorManager moderatorManager;
    private final GetCourseService getCourseService;

    @Override
    @Transactional
    public CourseDto updateCourse(UpdateCourseRequest request, Long courseId, Long userId) {
        Course course = getCourseService.getCourseByIdOrElseThrow(courseId);

        validateAuthorAccess(course, userId);
        validateCourseEditable(course);

        applyUpdates(course, request);
        sendToModeration(course);

        log.info("Course updated successfully: {}", course);
        return courseMapper.toDto(course);
    }

    private void validateAuthorAccess(Course course, Long userId) {
        if (!course.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("User is not the author!");
        }
    }

    private void validateCourseEditable(Course course) {
        if (course.getStatus() != CourseStatus.NOT_ACCEPTED) {
            throw new IllegalStateException("Can update course only after failed moderation");
        }
    }

    private void applyUpdates(Course course, UpdateCourseRequest request) {
        CourseBusinessDetails businessDetails = course.getBusinessDetails();
        Optional.ofNullable(request.title()).ifPresent(businessDetails::setTitle);
        Optional.ofNullable(request.courseText()).ifPresent(businessDetails::setCourseText);
        Optional.ofNullable(request.description()).ifPresent(businessDetails::setDescription);
        Optional.ofNullable(request.tags())
                .filter(tags -> tags.length > 0)
                .ifPresent(businessDetails::setTags);
    }

    private void sendToModeration(Course course) {
        course.setModerator(moderatorManager.assignModerator());
        course.setStatus(CourseStatus.ON_MODERATION);
    }
}
