package ru.tbank.knowhow.service.course;

import ru.tbank.knowhow.model.dto.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

public interface UpdateCourseService {

    CourseDto updateCourse(UpdateCourseRequest request, Long courseId, Long userId);
}
