package ru.tbank.knowhow.service.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.repository.CourseRepository;
import java.math.BigDecimal;

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
        log.debug("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        courseRepository.delete(course);
        log.debug("Course deleted successfully: {}", id);
    }
    @Transactional
    public void updateCourseRating(Long courseId, Double newRating) {
        log.debug("Updating course rating for course id: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));

        course.setRating(BigDecimal.valueOf(newRating));
        courseRepository.save(course);
        log.debug("Course rating updated successfully: {} -> {}", courseId, newRating);
    }
}
