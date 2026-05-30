package ru.tbank.knowhow.core_service.service.courses;

import ru.tbank.knowhow.core_service.model.dto.course.request.UpdateCourseRequest;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

public interface UpdateCourseService {

    CourseDto updateCourse(UpdateCourseRequest request, Long courseId, Long userId);
}
