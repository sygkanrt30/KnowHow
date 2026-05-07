package ru.tbank.knowhow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Course;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>{

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

    @Query(value = """
        SELECT c.* FROM course c
        INNER JOIN purchased_course pc ON c.id = pc.course_id
        WHERE pc.user_id = :userId AND pc.course_id = :courseId
        """, nativeQuery = true)
    Optional<Course> findPurchasedCourseByUserAndCourseId(@Param("userId") Long userId, @Param("courseId")  Long courseId);

    @Query(value = """
        INSERT INTO purchased_course (course_id, user_id)
        VALUES (:courseId, :userId)
        """, nativeQuery = true)
    @Modifying
    void insertCourseToPurchased(@Param("userId") Long userId, @Param("courseId")  Long courseId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM purchased_course WHERE course_id = :courseId AND user_id = :userId)", nativeQuery = true)
    boolean existsPurchasedCourse(@Param("courseId") Long courseId, @Param("userId") Long userId);
           
    @Query(value = """
        DELETE FROM purchased_course
        WHERE user_id = :userId
        """, nativeQuery = true)
    @Modifying
    void deleteAllPurchasedCoursesByUserId(Long userId);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM purchased_course WHERE course_id = :courseId)", nativeQuery = true)
    boolean existsPurchasedCourseByCourseId(@Param("courseId") Long courseId);

    @Query(value = """
    SELECT * FROM (
        SELECT c.*,
               CASE WHEN c.tags @> CAST(:tags AS varchar[]) THEN 1
                    ELSE 2
               END as match_priority
        FROM course c
        WHERE c.not_for_sale = false
          AND (CAST(:tags AS varchar[]) IS NULL OR cardinality(CAST(:tags AS varchar[])) = 0 OR c.tags && CAST(:tags AS varchar[]))
          AND (:title IS NULL OR c.title ILIKE CONCAT('%', :title, '%'))
          AND (:authorName IS NULL OR EXISTS (
              SELECT 1 FROM app_user u
              WHERE u.id = c.user_id
                AND u.username ILIKE CONCAT('%', :authorName, '%')
          ))
          AND (:minPrice IS NULL OR c.price >= :minPrice)
          AND (:maxPrice IS NULL OR c.price <= :maxPrice)
    ) AS sorted
    ORDER BY match_priority , sorted.created_at DESC
    """,
            countQuery = """
    SELECT COUNT(*) FROM course c
    WHERE c.not_for_sale = false
      AND (CAST(:tags AS varchar[]) IS NULL OR cardinality(CAST(:tags AS varchar[])) = 0 OR c.tags && CAST(:tags AS varchar[]))
      AND (:title IS NULL OR c.title ILIKE CONCAT('%', :title, '%'))
      AND (:authorName IS NULL OR EXISTS (
          SELECT 1 FROM app_user u
          WHERE u.id = c.user_id
            AND u.username ILIKE CONCAT('%', :authorName, '%')
      ))
      AND (:minPrice IS NULL OR c.price >= :minPrice)
      AND (:maxPrice IS NULL OR c.price <= :maxPrice)
    """,
            nativeQuery = true)
    Page<Course> searchCourses(
            @Param("tags") String[] tags,
            @Param("title") String title,
            @Param("authorName") String authorName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = "SELECT tags FROM course", nativeQuery = true)
    Stream<String[]> getTags();
}
