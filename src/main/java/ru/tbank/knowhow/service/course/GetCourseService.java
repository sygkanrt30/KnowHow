package ru.tbank.knowhow.service.course;

import org.springframework.data.domain.Page;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

public interface GetCourseService {

    Page<CourseDto> findAllPurchasedCourses(Long userId, int page, int size, SortRequest sortRequest);

    CourseDto createCourse(CreateCourseRequest request, Long authorId);
}
