package ru.tbank.knowhow.service.rating;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.course.CourseService;
import ru.tbank.knowhow.service.user.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService {

    private final CourseRepository courseRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final CourseService courseService;
    private final UserService userService;

    @Transactional
    public boolean addRating(Long courseId, Integer grade, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Курс не найден"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        boolean alreadyRated = ratingRepository.existsByCourseAndUser(course, user);
        if (alreadyRated) {
            return false;
        }

        Rating rating = Rating.builder()
                .grade(grade.shortValue())
                .course(course)
                .user(user)
                .build();

        ratingRepository.save(rating);
        log.debug("Rating saved: user={}, course={}, grade={}", username, courseId, grade);

        Double avgRating = ratingRepository.getAverageRatingForCourse(courseId);
        if (avgRating == null) avgRating = 0.0;
        courseService.updateCourseRating(courseId, avgRating);

        userService.recalculateTeacherLevel(course.getAuthor().getId());

        return true;
    }
}
