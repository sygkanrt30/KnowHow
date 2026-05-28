package ru.tbank.knowhow.service.purchased;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.purchase.PurchasedCourse;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.mappers.CourseMapper;
import ru.tbank.knowhow.repository.PurchasedCourseRepository;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class PurchasedCourseServiceImpl implements PurchasedCourseService {

    private final PurchasedCourseRepository purchasedCourseRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public List<CourseDto> findAllPurchasedCourses(Long userId) {
        return purchasedCourseRepository.findPurchasedCoursesByUserId(userId)
                .stream()
                .map(PurchasedCourse::getCourse)
                .map(courseMapper::toDto)
                .toList();
    }

    @Override
    public void deleteAllPurchasedCoursesByUserId(Long userId) {
        purchasedCourseRepository.deleteAllPurchasedCoursesByUserId(userId);
        log.info("PurchasedCourses deleted successfully");
    }

    @Override
    public boolean existsPurchasedCourseByCourseId(Long courseId) {
        return purchasedCourseRepository.existsPurchasedCourseByCourseId(courseId);
    }

    @Override
    public boolean existsPurchasedCourse(Long courseId, Long userId) {
        return purchasedCourseRepository.existsPurchasedCourseByCourseIdAndUserId(courseId, userId);
    }
}
