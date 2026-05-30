package ru.tbank.knowhow.core_service.service.purchase;

import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

public interface CoursePurchaseService {

    CourseDto payForCourse(Long courseId, Long userId);
}
