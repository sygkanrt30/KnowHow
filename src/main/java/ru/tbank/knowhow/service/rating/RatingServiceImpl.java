package ru.tbank.knowhow.service.rating;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final CourseRepository courseRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
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
        return true;
    }
}
