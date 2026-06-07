package ru.tbank.knowhow.core_service.service.purchase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.core_service.mappers.CourseMapper;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.model.purchase.PurchasedCourse;
import ru.tbank.knowhow.core_service.model.purchase.PurchasedCourseId;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.repository.PurchasedCourseRepository;
import ru.tbank.knowhow.core_service.service.courses.GetCourseService;
import ru.tbank.knowhow.core_service.service.event.NotificationEventPublisher;
import ru.tbank.knowhow.core_service.service.users.GetUserService;
import ru.tbank.knowhow.core_service.service.users.balance.CoinsRefresher;

@Service
@Slf4j
public class PurchaseCourseServiceImpl implements CoursePurchaseService {

    private final CourseMapper courseMapper;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final GetUserService getUserService;
    private final GetCourseService getCourseService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final CoinsRefresher coinsRefresher;
    private final PurchasePreconditionValidator validator;

    public PurchaseCourseServiceImpl(CourseMapper courseMapper,
                                     PurchasedCourseRepository purchasedCourseRepository,
                                     GetUserService getUserService,
                                     GetCourseService getCourseService,
                                     NotificationEventPublisher notificationEventPublisher) {

        this.courseMapper = courseMapper;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.coinsRefresher = new CoinsRefresher();
        this.getUserService = getUserService;
        this.getCourseService = getCourseService;
        this.notificationEventPublisher = notificationEventPublisher;
        this.validator = new PurchasePreconditionValidator();
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CourseDto payForCourse(Long courseId, Long userId) {
        Course course = getCourseService.getCourseByIdOrElseThrow(courseId);
        User user = getUserService.getByIdOrElseThrow(userId);
        User author = course.getAuthor();
        validator.validate(course, user, author);

        if (purchasedCourseRepository.findByIdCourseIdAndIdUserId(userId, courseId).isPresent()) {
            log.warn("Course with id: {} has already been purchased", courseId);
            return courseMapper.toDto(course);
        }

        processPayment(course, user, author);
        PurchasedCourse purchasedCourse = createPurchasedCourse(courseId, userId, user, course);
        purchasedCourseRepository.saveAndFlush(purchasedCourse);
        log.info("Course with id: {} has been purchased", courseId);
        notificationEventPublisher.createAndPublishCoursePurchaseEvent(
                user.getUserContact(),
                author.getUsername(),
                courseId
        );
        return courseMapper.toDto(course);
    }

    private void processPayment(Course course, User user, User author) {
        long price = course.getBusinessDetails().getPrice();
        validator.validateSufficientFunds(user.getBalance().getCoins(), price);
        coinsRefresher.decrease(user.getBalance(), price);
        coinsRefresher.increase(author.getBalance(), price);
    }

    private PurchasedCourse createPurchasedCourse(Long courseId, Long userId, User user, Course course) {
        var id = PurchasedCourseId.builder()
                .courseId(courseId)
                .userId(userId)
                .build();
        return new PurchasedCourse(id, course, user);
    }
}
