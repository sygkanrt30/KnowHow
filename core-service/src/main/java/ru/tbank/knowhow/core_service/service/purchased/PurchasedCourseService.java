package ru.tbank.knowhow.core_service.service.purchased;

import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

import java.util.List;

public interface PurchasedCourseService {

    List<CourseDto> findAllPurchasedCourses(Long userId);

    void deleteAllPurchasedCoursesByUserId(Long userId);

    boolean existsPurchasedCourseByCourseId(Long courseId);

    boolean existsPurchasedCourse(Long courseId, Long userId);
}
