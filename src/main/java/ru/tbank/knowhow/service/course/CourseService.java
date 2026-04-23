package ru.tbank.knowhow.service.course;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.ecxeption.AttemptPayForYourselfException;
import ru.tbank.knowhow.ecxeption.InsufficientFundsException;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.ModeratorLoad;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.request.SortRequest;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.repository.ModeratorLoadRepository;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.repository.UserRepository;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService, PurchaseCourseService, CreateCourseService {

    private final CourseRepository courseRepository;
    private final GetUserInfoService getUserInfoService;
    private final CourseMapper courseMapper;
    private final UserRepository userRepository;
    private final ModeratorLoadRepository moderatorLoadRepository;
    private final RatingRepository ratingRepository;
    private final Long priceMultiplier;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         GetUserInfoService getUserInfoService,
                         CourseMapper courseMapper,
                         UserRepository userRepository,
                         ModeratorLoadRepository moderatorLoadRepository,
                         RatingRepository ratingRepository,
                         @Value("${course.price-multiplier:20}") Long priceMultiplier) {
        this.courseRepository = courseRepository;
        this.getUserInfoService = getUserInfoService;
        this.courseMapper = courseMapper;
        this.userRepository = userRepository;
        this.moderatorLoadRepository = moderatorLoadRepository;
        this.ratingRepository = ratingRepository;
        this.priceMultiplier = priceMultiplier;
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
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        User moderator = assignModerator();

        int userLevel = author.getLevel() != null ? author.getLevel() : 1;
        Long price = priceMultiplier * userLevel;

        Course course = courseMapper.toEntity(request, author, moderator, price);
        course.setStatus(CourseStatus.ON_MODERATION);
        course.setRating(BigDecimal.ZERO);

        Course saved = courseRepository.save(course);
        return courseMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CourseDto payForCourse(Long courseId, Long userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + courseId));
        User user = getUserInfoService.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));

        User author = course.getAuthor();
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

    private User assignModerator() {
        ModeratorLoad moderatorLoad = moderatorLoadRepository.findModeratorWithMinLoad()
                .orElseThrow(() -> new EntityNotFoundException("No moderators available"));

        moderatorLoadRepository.incrementCoursesInModeration(moderatorLoad.getModerator().getId());

        return moderatorLoad.getModerator();
    }
}