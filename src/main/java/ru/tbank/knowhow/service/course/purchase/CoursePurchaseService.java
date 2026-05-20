package ru.tbank.knowhow.service.course.purchase;

import ru.tbank.knowhow.model.dto.response.CourseDto;

public interface CoursePurchaseService {

    CourseDto payForCourse(Long courseId, Long userId);
}
