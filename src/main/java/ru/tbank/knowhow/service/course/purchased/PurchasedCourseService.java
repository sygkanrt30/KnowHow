package ru.tbank.knowhow.service.course.purchased;

import ru.tbank.knowhow.model.dto.response.CourseDto;

import java.util.List;

public interface PurchasedCourseService {

    List<CourseDto> findAllPurchasedCourses(Long userId);

    void deleteAllPurchasedCoursesByUserId(Long userId);

    boolean existsPurchasedCourseByCourseId(Long courseId);

    boolean existsPurchasedCourse(Long courseId, Long userId);
}
