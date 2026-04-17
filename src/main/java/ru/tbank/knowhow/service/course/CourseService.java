package ru.tbank.knowhow.service.course;

import jakarta.persistence.EntityNotFoundException;
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

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    @Autowired
    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

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
    public Page<CourseDto> findAllPurchasedCourses(Long userId, int page, int size, SortRequest sortRequest) {
        Sort sort = getSort(sortRequest);
        var pageable = PageRequest.of(page, size, sort);
        return courseRepository.findPurchasedCoursesByUserId(userId, pageable)
                .map(courseMapper::toDto);
    }

    private Sort getSort(SortRequest sortRequest) {
        if (sortRequest.isACS()) {
            return Sort.by(Sort.Direction.ASC, sortRequest.fieldName().propertyName());
        }
        return Sort.by(Sort.Direction.DESC, sortRequest.fieldName().propertyName());
    }
}