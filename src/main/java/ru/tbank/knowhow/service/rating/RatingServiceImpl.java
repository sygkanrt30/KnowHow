package ru.tbank.knowhow.service.rating;

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

import java.util.Optional;

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
    public void insertRating(Long courseId, Long userId, Integer grade) {
        Optional<Rating> ratingOptional = ratingRepository.findByCourseIdAndUserId(courseId, userId);
        if (ratingOptional.isPresent()) {
            ratingOptional.get().setGrade(grade.shortValue());
            return;
        }

        Course course = getCourseService.getCourseByIdOrElseThrow(courseId);
        User user = getUserService.getByIdOrElseThrow(userId);
        throwIfCourseNotPurchased(courseId, user);

        var rating = Rating.builder()
                .grade(grade.shortValue())
                .course(course)
                .user(user)
                .build();
        ratingRepository.save(rating);
    }

    private void throwIfCourseNotPurchased(Long courseId, User user) {
        boolean hasPurchased = purchasedCourseService.existsPurchasedCourse(courseId, user.getId());
        if (!hasPurchased) {
            throw new IllegalStateException("You can not rate course without purchasing");
        }
    }
}
