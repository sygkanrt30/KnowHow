package ru.tbank.knowhow.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.AttemptPayForYourselfException;
import ru.tbank.knowhow.ecxeption.AttemptPayNotForSaleCourseException;
import ru.tbank.knowhow.ecxeption.InsufficientFundsException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CourseSearchRequest;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.UpdateCourseRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.service.balance.CoinsRefresher;
import ru.tbank.knowhow.service.moder.ModerationService;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService, PurchaseCourseService, SaveCourseService {

    private final CourseRepository courseRepository;
    private final GetUserInfoService getUserInfoService;
    private final CourseMapper courseMapper;
    private final ModerationService moderationService;
    private final int priceMultiplier;
    private final CoinsRefresher coinsRefresher;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         GetUserInfoService getUserInfoService,
                         CourseMapper courseMapper,
                         ModerationService moderationService,
                         @Value("${course.price-multiplier}") int priceMultiplier) {
        this.courseRepository = courseRepository;
        this.getUserInfoService = getUserInfoService;
        this.courseMapper = courseMapper;
        this.moderationService = moderationService;
        this.priceMultiplier = priceMultiplier;
        this.coinsRefresher = new CoinsRefresher();
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));

        boolean isPurchased = courseRepository.existsPurchasedCourseByCourseId(id);
        if (isPurchased) {
            throw new IllegalStateException("Cannot delete course that has already been purchased");
        }
        courseRepository.delete(course);
    }

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, String username) {
        User author = getUserInfoService.getByUsernameOrElseThrow(username);

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

        if (course.isNotForSale()) {
            throw new AttemptPayNotForSaleCourseException("Course not for sale");
        }

        User user = getUserInfoService.getByIdOrElseThrow(userId);

        User author = course.getAuthor();
        if (course.getStatus().equals(CourseStatus.ON_MODERATION)) {
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
        coinsRefresher.decrease(user.getBalance(), price);
        coinsRefresher.increase(author.getBalance(), price);
        courseRepository.insertCourseToPurchased(userId, courseId);
        log.info("Course with id: {} has been purchased", courseId);
        return courseMapper.toDto(course);
    }

    @Override
    public List<CourseDto> findAllPurchasedCourses(Long userId) {
        return courseRepository.findPurchasedCoursesByUserId(userId)
                .stream()
                .map(courseMapper::toDto)
                .toList();
    }

    @Override
    public Page<CourseDto> searchCourses(CourseSearchRequest request, Pageable pageable) {
        log.debug("Searching courses with filters: {}", request);

        String[] tagsArray = request.tags();
        Page<Course> coursePage = courseRepository.searchCourses(
                tagsArray,
                request.title(),
                request.authorName(),
                request.minPrice(),
                request.maxPrice(),
                pageable
        );
        return coursePage.map(courseMapper::toDto);
    }

    @Override
    public CourseDto findCourseById(Long id) {
        return courseMapper.toDto(
                courseRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id))
        );
    }

    @Override
    @Transactional
    public Stream<String[]> findAllTags() {
        return courseRepository.getTags();
    }
}