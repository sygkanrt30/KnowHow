package ru.tbank.knowhow.service.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.CourseException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.repository.CourseRepository;

@Service
@Slf4j
public class CourseService implements DeleteCourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.info("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> CourseException.notFound(String.valueOf(id)));

        courseRepository.delete(course);
        log.info("Course deleted successfully: {}", id);
    }
}