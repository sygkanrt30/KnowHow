package ru.tbank.knowhow.service.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.model.dto.request.CourseSearchRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

import java.util.List;

public interface GetCourseService {

    List<CourseDto> findAllPurchasedCourses(Long userId);

    Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable);

    CourseDto findCourseById(Long id);
}
