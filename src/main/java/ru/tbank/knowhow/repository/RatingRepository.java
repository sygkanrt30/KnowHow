package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    boolean existsByCourseAndUser(Course course, User user);
}
