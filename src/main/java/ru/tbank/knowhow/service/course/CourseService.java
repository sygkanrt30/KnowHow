package ru.tbank.knowhow.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService {

    private final CourseRepository courseRepository;
    private final GetUserInfoService getUserInfoService;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        courseRepository.delete(course);
        log.debug("Course deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void payForCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        User user = getUserInfoService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        if (course.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException("You cannot purchase your own course");
        }
        if (user.getPurchasedCourses().stream().anyMatch(purchased -> purchased.getId().equals(courseId))) {
            throw new IllegalStateException("Course is already purchased");
        }

        long currentCoins = user.getBalance().getCoins();
        if (currentCoins < course.getPrice()) {
            throw new IllegalStateException("Insufficient balance to buy this course");
        }

        user.getBalance().setCoins(currentCoins - course.getPrice());
        user.addPurchasedCourse(course);
      
    public Page<CourseDto> findAllPurchasedCourses(Long userId, int page, int size, SortRequest sortRequest) {
        Sort sort = getSort(sortRequest);
        var pageable = PageRequest.of(page, size, sort);
        return courseRepository.findPurchasedCoursesByUserId(userId, pageable)
                .map(courseMapper::toDto);
    }

    @Override
    public List<CourseDto> findAllPurchasedCourses(Long userId) {
        return courseRepository.findPurchasedCoursesByUserId(userId)
                .stream()
                .map(courseMapper::toDto)
                .toList();
    }

    private Sort getSort(SortRequest sortRequest) {
        if (sortRequest.isACS()) {
            return Sort.by(Sort.Direction.ASC, sortRequest.fieldName().propertyName());
        }
        return Sort.by(Sort.Direction.DESC, sortRequest.fieldName().propertyName());
    }
}