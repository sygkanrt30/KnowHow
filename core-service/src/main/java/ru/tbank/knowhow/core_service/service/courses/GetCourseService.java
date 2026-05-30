package ru.tbank.knowhow.core_service.service.courses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.course.request.CourseSearchRequest;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

import java.util.List;
import java.util.stream.Stream;

public interface GetCourseService {

    Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable);

    CourseDto getCourseDtoByIdOrElseThrow(Long id);

    Course getCourseByIdOrElseThrow(Long id);

    Stream<String[]> findAllTags();

    List<Course> findAllByModerator(User moderator);
}
