package ru.tbank.knowhow.service.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.service.user.GetUserInfoService;

@Service
@Slf4j
public class CourseService implements DeleteCourseService {

    private final CourseRepository courseRepository;
    private final GetUserInfoService getUserInfoService;

    @Autowired
    public CourseService(CourseRepository courseRepository, GetUserInfoService getUserInfoService) {
        this.courseRepository = courseRepository;
        this.getUserInfoService = getUserInfoService;
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

    @Override
    @Transactional
    public void payForCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        User user = getUserInfoService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        if (course.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("You cannot purchase your own course");
        }
        if (user.getPurchasedCourses().stream().anyMatch(purchased -> purchased.getId().equals(courseId))) {
            throw new IllegalStateException("Course is already purchased");
        }

        long currentCoins = user.getBalance().getCoins();
        if (currentCoins < course.getPrice()) {
            throw new IllegalStateException("Insufficient balance to buy this course");
        }

        user.getBalance().setCoins(currentCoins - course.getPrice());
        user.addPurchasedCourse(course);
    }
}