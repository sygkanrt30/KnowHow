package ru.tbank.knowhow.service.purchase;

import ru.tbank.knowhow.model.dto.course.response.CourseDto;

public interface CoursePurchaseService {

    CourseDto payForCourse(Long courseId, Long userId);
}
