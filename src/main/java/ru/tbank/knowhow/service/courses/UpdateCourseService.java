package ru.tbank.knowhow.service.courses;

import ru.tbank.knowhow.model.dto.course.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;

public interface UpdateCourseService {

    CourseDto updateCourse(UpdateCourseRequest request, Long courseId, Long userId);
}
