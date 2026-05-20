package ru.tbank.knowhow.service.rating;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.course.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final PurchasedCourseService purchasedCourseService;
    private final RatingRepository ratingRepository;
    private final GetUserService getUserService;
    private final GetCourseService getCourseService;

    @Override
    @Transactional
    public boolean addRating(Long courseId, Integer grade, String username) {
        Course course = getCourseService.getCourseByIdOrElseThrow(courseId);
        User user = getUserService.getByUsernameOrElseThrow(username);

        boolean hasPurchased = purchasedCourseService.existsPurchasedCourse(courseId, user.getId());
        if (!hasPurchased) {
            log.debug("User {} tried to rate course {} without purchasing", username, courseId);
            return false;
        }

        boolean alreadyRated = ratingRepository.existsByCourseAndUser(course, user);
        if (alreadyRated) {
            return false;
        }

        var rating = Rating.builder()
                .grade(grade.shortValue())
                .course(course)
                .user(user)
                .build();
        ratingRepository.save(rating);
        return true;
    }

    @Override
    @Transactional
    public void updateRating(Long courseId, Long userId, Integer newGrade) {
        Rating rating = ratingRepository.findByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new EntityNotFoundException("You haven't evaluated this course."));

        rating.setGrade(newGrade.shortValue());
        log.debug("User {} updated rating for course {} to {}", userId, courseId, newGrade);
    }
}
