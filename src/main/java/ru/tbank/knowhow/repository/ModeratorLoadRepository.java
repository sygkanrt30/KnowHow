package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tbank.knowhow.model.ModeratorLoad;

import java.util.Optional;

public interface ModeratorLoadRepository extends JpaRepository<ModeratorLoad, Long> {

    @Query("SELECT ml FROM ModeratorLoad ml ORDER BY ml.coursesInModeration ASC LIMIT 1")
    Optional<ModeratorLoad> findModeratorWithMinLoad();

    @Modifying
    @Query("UPDATE ModeratorLoad ml SET ml.coursesInModeration = ml.coursesInModeration + 1 WHERE ml.moderator.id = :moderatorId")
    void incrementCoursesInModeration(@Param("moderatorId") Long moderatorId);
}
