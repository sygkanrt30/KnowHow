package ru.tbank.knowhow.service.courses;

import ru.tbank.knowhow.model.dto.course.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;

public interface SaveCourseService {

    CourseDto createCourse(CreateCourseRequest request, String username);
}
