package ru.tbank.knowhow.core_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import ru.tbank.knowhow.core_service.model.purchase.PurchasedCourse;
import ru.tbank.knowhow.core_service.model.purchase.PurchasedCourseId;

import java.util.List;
import java.util.Optional;

public interface PurchasedCourseRepository extends JpaRepository<PurchasedCourse, PurchasedCourseId> {

    Optional<PurchasedCourse> findByIdCourseIdAndIdUserId(Long courseId, Long userId);

    List<PurchasedCourse> findPurchasedCoursesByUserId(Long userId);

    boolean existsPurchasedCourseByCourseIdAndUserId(Long courseId, Long userId);

    @Modifying
    void deleteAllPurchasedCoursesByUserId(Long userId);

    boolean existsPurchasedCourseByCourseId(Long courseId);
}
