package ru.tbank.knowhow.service.course;

import ru.tbank.knowhow.model.dto.response.CourseDto;

public interface PurchaseCourseService {

    CourseDto payForCourse(Long courseId, Long userId);
}
