package ru.tbank.knowhow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Course;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByAuthorId(Long authorId);
  
    @Query(value = """
        SELECT c.* FROM course c
        INNER JOIN purchased_course pc ON c.id = pc.course_id
        WHERE pc.user_id = :userId
        """,
            countQuery = """
        SELECT COUNT(*) FROM purchased_course WHERE user_id = :userId
        """,
            nativeQuery = true)
    Page<Course> findPurchasedCoursesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
        SELECT c.* FROM course c
        INNER JOIN purchased_course pc ON c.id = pc.course_id
        WHERE pc.user_id = :userId
        """, nativeQuery = true)
    List<Course> findPurchasedCoursesByUserId(@Param("userId") Long userId);
}
