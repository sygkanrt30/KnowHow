package ru.tbank.knowhow.service.courses;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.model.dto.course.request.CourseSearchRequest;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;

import java.util.List;
import java.util.stream.Stream;

public interface GetCourseService {

    Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable);

    CourseDto getCourseDtoByIdOrElseThrow(Long id);

    Course getCourseByIdOrElseThrow(Long id);

    Stream<String[]> findAllTags();

    List<Course> findAllByModerator(User moderator);
}
