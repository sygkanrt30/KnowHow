package ru.tbank.knowhow.core_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.users.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @Query(value = """
            WITH params AS (
                SELECT CAST(:tags AS varchar[]) as tags_array
            )
            SELECT * FROM (
               SELECT c.*,
                      CASE WHEN p.tags_array IS NOT NULL
                                AND cardinality(p.tags_array) > 0
                                AND bd.tags @> p.tags_array
                           THEN 1
                           ELSE 2
                      END as match_priority
               FROM course c
               JOIN business_details bd ON c.business_details_id = bd.id
               CROSS JOIN params p
               WHERE c.not_for_sale = false
                 AND c.status = 'PASSED_MODERATION'
                 AND (p.tags_array IS NULL
                      OR cardinality(p.tags_array) = 0
                      OR bd.tags && p.tags_array)
                 AND (:title IS NULL OR :title = '' OR bd.title ILIKE CONCAT('%', :title, '%'))
                 AND (:authorName IS NULL OR :authorName = '' OR EXISTS (
                     SELECT 1 FROM app_user u
                     WHERE u.id = c.user_id
                       AND u.username ILIKE CONCAT('%', :authorName, '%')
                 ))
                 AND (:minPrice IS NULL OR bd.price >= :minPrice)
                 AND (:maxPrice IS NULL OR bd.price <= :maxPrice)
            ) AS sorted
            ORDER BY match_priority
            """,
            countQuery = """
                    SELECT COUNT(*) FROM course c
                    JOIN business_details bd
                    ON c.business_details_id = bd.id
                    WHERE c.not_for_sale = false
                      AND c.status = 'PASSED_MODERATION'
                      AND (CAST(:tags AS varchar[]) IS NULL
                           OR cardinality(CAST(:tags AS varchar[])) = 0
                           OR bd.tags && CAST(:tags AS varchar[]))
                      AND (:title IS NULL OR :title = '' OR bd.title ILIKE CONCAT('%', :title, '%'))
                      AND (:authorName IS NULL OR :authorName = '' OR EXISTS (
                          SELECT 1 FROM app_user u
                          WHERE u.id = c.user_id
                            AND u.username ILIKE CONCAT('%', :authorName, '%')
                      ))
                      AND (:minPrice IS NULL OR bd.price >= :minPrice)
                      AND (:maxPrice IS NULL OR bd.price <= :maxPrice)
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

    @Query(value = """
            SELECT tags FROM course
            JOIN business_details
            ON course.business_details_id = business_details.id
            """, nativeQuery = true)
    Stream<String[]> getTags();

    List<Course> findAllByModerator(User moderator);
}
