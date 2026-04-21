package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByAuthorId(Long authorId);
}
