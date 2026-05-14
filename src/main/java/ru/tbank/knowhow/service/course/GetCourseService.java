package ru.tbank.knowhow.service.course;

import org.springframework.data.domain.Page;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.model.dto.request.CourseSearchRequest;

import java.util.List;

public interface GetCourseService {

    Page<CourseDto> findAllPurchasedCourses(Long userId, int page, int size, SortRequest sortRequest);

    List<CourseDto> findAllPurchasedCourses(Long userId);

    Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable);

    CourseDto findCourseById(Long id);
}
