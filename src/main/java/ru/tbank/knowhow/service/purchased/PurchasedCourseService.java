package ru.tbank.knowhow.service.purchased;

import ru.tbank.knowhow.model.dto.course.response.CourseDto;

import java.util.List;

public interface PurchasedCourseService {

    List<CourseDto> findAllPurchasedCourses(Long userId);

    void deleteAllPurchasedCoursesByUserId(Long userId);

    boolean existsPurchasedCourseByCourseId(Long courseId);

    boolean existsPurchasedCourse(Long courseId, Long userId);
}
