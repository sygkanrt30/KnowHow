package ru.tbank.knowhow.core_service.service.courses;

import ru.tbank.knowhow.core_service.model.dto.course.request.CreateCourseRequest;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

public interface SaveCourseService {

    CourseDto createCourse(CreateCourseRequest request, String username);
}
