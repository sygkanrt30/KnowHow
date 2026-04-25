package ru.tbank.knowhow.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.AttemptPayForYourselfException;
import ru.tbank.knowhow.ecxeption.InsufficientFundsException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.moder.ModerationService;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService, PurchaseCourseService, SaveCourseService {

    private final CourseRepository courseRepository;
    private final GetUserInfoService getUserInfoService;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final ModerationService moderationService;
    private final RatingRepository ratingRepository;
    private final int priceMultiplier;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         GetUserInfoService getUserInfoService,
                         CourseMapper courseMapper,
                         UserRepository userRepository,
                         ModerationService moderationService,
                         RatingRepository ratingRepository,
                         @Value("${course.price-multiplier}") int priceMultiplier) {
        this.courseRepository = courseRepository;
        this.getUserInfoService = getUserInfoService;
        this.courseMapper = courseMapper;
        this.userRepository = userRepository;
        this.moderationService = moderationService;
        this.ratingRepository = ratingRepository;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        ratingRepository.deleteByCourseId(id);
        log.debug("Deleted all ratings for course id: {}", id);

        courseRepository.delete(course);
        log.debug("Course deleted successfully: {}", id);
    }

    @Override
    public CourseDto createCourse(CreateCourseRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        User moderator = moderationService.assignModerator();

        int userLevel = author.getLevel() != null ? author.getLevel() : 1;
        Integer price = priceMultiplier * userLevel;
        Course course = courseMapper.toEntity(request, author, moderator, price);
        Course saved = courseRepository.save(course);
        return courseMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CourseDto updateCourse(UpdateCourseRequest request, Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        if (!course.getAuthor().getId().equals(userId)) {
            throw new AccessDeniedException("User is not the author!");
        }
        if (!course.getStatus().equals(CourseStatus.NOT_ACCEPTED)) {
            throw new IllegalStateException("Can update course only after failed moderation");
        }
        if (Objects.nonNull(request.title())) {
            course.setTitle(request.title());
        }
        if (Objects.nonNull(request.courseText())) {
            course.setCourseText(request.courseText());
        }
        if (Objects.nonNull(request.description())) {
            course.setDescription(request.description());
        }
        if (Objects.nonNull(request.tags()) && request.tags().length > 0) {
            course.setTags(request.tags());
        }
        User moderator = moderationService.assignModerator();
        course.setModerator(moderator);
        course.setStatus(CourseStatus.ON_MODERATION);
        log.info("Course updated successfully: {}", course);
        return courseMapper.toDto(course);
    }

    @Override
    @Transactional
    public CourseDto payForCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        User user = getUserInfoService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        User author = course.getAuthor();
        if (course.getStatus().equals(CourseStatus.PASSED_MODERATION)) {
            throw new IllegalStateException("Can not pay for moderated courses!");
        }
        if (author.getId().equals(user.getId())) {
            throw new AttemptPayForYourselfException("You can't pay for yourself!");
        }
        if (courseRepository.findPurchasedCourseByUserAndCourseId(userId, courseId).isPresent()) {
            log.warn("Course with id: {} has already been purchased", courseId);
            return courseMapper.toDto(course);
        }
        long coinsBalance = user.getBalance().getCoins();
        long price = course.getPrice();
        if (coinsBalance <= 0L || coinsBalance < price) {
            throw new InsufficientFundsException("Insufficient funds!");
        }
        user.getBalance().setCoins(user.getBalance().getCoins() - price);
        author.getBalance().setCoins(author.getBalance().getCoins() + price);
        courseRepository.insertCourseToPurchased(userId, courseId);
        log.info("Course with id: {} has been purchased", courseId);
        return courseMapper.toDto(course);
    }

    @Override
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