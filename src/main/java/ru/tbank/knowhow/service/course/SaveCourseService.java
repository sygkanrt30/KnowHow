package ru.tbank.knowhow.service.course;

import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

public interface SaveCourseService {

    CourseDto createCourse(CreateCourseRequest request, String username);
}
