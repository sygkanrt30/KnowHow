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
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.repository.ModeratorLoadRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.model.ModeratorLoad;

import java.math.BigDecimal;

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final ModeratorLoadRepository moderatorLoadRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         CourseMapper courseMapper,
                         UserRepository userRepository,
                         ModeratorLoadRepository moderatorLoadRepository,
                         RatingRepository ratingRepository) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
        this.userRepository = userRepository;
        this.moderatorLoadRepository = moderatorLoadRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        log.debug("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        ratingRepository.deleteByCourseId(id);
        log.debug("Deleted all ratings for course id: {}", id);

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

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, Long authorId) {
        log.debug("Creating course for author id: {}", authorId);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + authorId));

        User moderator = assignModerator();
        log.debug("Assigned moderator id: {}", moderator.getId());

        int userLevel = author.getLevel() != null ? author.getLevel() : 1;
        Long price = 20L * userLevel;
        log.debug("Calculated price: {} (user level: {})", price, userLevel);

        Course course = new Course();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setCourseText(request.courseText());
        course.setTags(request.tags() != null ? request.tags() : new String[0]);
        course.setPrice(price);
        course.setStatus(CourseStatus.ON_MODERATION);
        course.setRating(BigDecimal.ZERO);
        course.setModerationScore(0);
        course.setAuthor(author);
        course.setModerator(moderator);

        Course saved = courseRepository.save(course);
        log.debug("Course created successfully with id: {}, price: {}", saved.getId(), price);

        return courseMapper.toDto(saved);
    }

    private Sort getSort(SortRequest sortRequest) {
        if (sortRequest.isACS()) {
            return Sort.by(Sort.Direction.ASC, sortRequest.fieldName().propertyName());
        }
        return Sort.by(Sort.Direction.DESC, sortRequest.fieldName().propertyName());
    }

    private User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }
}