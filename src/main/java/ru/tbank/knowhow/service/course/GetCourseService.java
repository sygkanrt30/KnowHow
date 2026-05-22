package ru.tbank.knowhow.service.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CourseSearchRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;

import java.util.List;
import java.util.stream.Stream;

public interface GetCourseService {

    Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable);

    CourseDto getCourseDtoByIdOrElseThrow(Long id);

    Course getCourseByIdOrElseThrow(Long id);

    Stream<String[]> findAllTags();

    List<Course> findAllByModerator(User moderator);
}
