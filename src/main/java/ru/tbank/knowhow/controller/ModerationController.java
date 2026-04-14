package ru.tbank.knowhow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.repository.UserRepository;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ModerationController {

    private final CourseRepository courseRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;

    @PostMapping("/courses/{id}/rating")
    public ResponseEntity<Void> addRating(
            @PathVariable Long id,
            @RequestParam Integer grade) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Курс не найден"));

        User user = userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Проверка: не ставил ли пользователь уже оценку
        boolean alreadyRated = ratingRepository.existsByCourseAndUser(course, user);
        if (alreadyRated) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Rating rating = Rating.builder()
                .grade(grade.shortValue())
                .course(course)
                .user(user)
                .build();

        ratingRepository.save(rating);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}