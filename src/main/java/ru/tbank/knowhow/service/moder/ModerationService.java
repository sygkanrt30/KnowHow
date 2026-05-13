package ru.tbank.knowhow.service.moder;

import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.CourseDto;

import java.util.List;

public interface ModerationService {

    User assignModerator();

    void approveCourse(Long courseId, String moderatorUsername);

    void rejectCourse(Long courseId, String moderatorUsername, String rejectionReason);

    List<CourseDto> findAllCoursesOnModeration(Long moderationId);
}
