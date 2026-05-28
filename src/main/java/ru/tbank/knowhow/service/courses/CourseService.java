package ru.tbank.knowhow.service.courses;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.model.dto.course.request.CourseSearchRequest;
import ru.tbank.knowhow.model.dto.course.request.CreateCourseRequest;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.mappers.CourseMapper;
import ru.tbank.knowhow.repository.CourseRepository;
import ru.tbank.knowhow.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.moderation.ModeratorManager;
import ru.tbank.knowhow.service.users.GetUserService;

import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class CourseService implements DeleteCourseService, GetCourseService, SaveCourseService {

    private final CourseRepository courseRepository;
    private final GetUserService getUserService;
    private final CourseMapper courseMapper;
    private final ModeratorManager moderatorManager;
    private final PurchasedCourseService purchasedCourseService;
    private final PriceCalculator priceCalculator;

    @Autowired
    public CourseService(CourseRepository courseRepository,
                         GetUserService getUserService,
                         CourseMapper courseMapper,
                         PurchasedCourseService purchasedCourseService,
                         ModeratorManager moderatorManager,
                         @Value("${course.price-multiplier}") int priceMultiplier) {

        this.courseRepository = courseRepository;
        this.getUserService = getUserService;
        this.courseMapper = courseMapper;
        this.moderatorManager = moderatorManager;
        this.purchasedCourseService = purchasedCourseService;
        this.priceCalculator = new PriceCalculator(priceMultiplier);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourse(id);
        throwIfCourseAlreadyBeenPurchased(id);
        courseRepository.delete(course);
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    private void throwIfCourseAlreadyBeenPurchased(Long id) {
        boolean isPurchased = purchasedCourseService.existsPurchasedCourseByCourseId(id);
        if (isPurchased) {
            throw new IllegalArgumentException("Cannot delete course that has already been purchased");
        }
    }

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, String username) {
        User author = getUserService.getByUsernameOrElseThrow(username);

        User moderator = moderatorManager.assignModerator();

        Integer price = priceCalculator.calculate(author.getLevel());
        Course course = courseMapper.toEntity(request, author, moderator, price);
        Course saved = courseRepository.save(course);
        return courseMapper.toDto(saved);
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
    public CourseDto getCourseDtoByIdOrElseThrow(Long id) {
        return courseMapper.toDto(getCourseByIdOrElseThrow(id));
    }

    @Override
    public Course getCourseByIdOrElseThrow(Long id) {
        return getCourse(id);
    }

    @Override
    @Transactional
    public Stream<String[]> findAllTags() {
        return courseRepository.getTags();
    }

    @Override
    public List<Course> findAllByModerator(User moderator) {
        return courseRepository.findAllByModerator(moderator);
    }
}