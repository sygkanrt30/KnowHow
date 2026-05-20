package ru.tbank.knowhow.service.course.purchase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.PurchasedCourse;
import ru.tbank.knowhow.model.PurchasedCourseId;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.PurchasedCourseRepository;
import ru.tbank.knowhow.service.balance.CoinsRefresher;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

@Service
@Slf4j
public class PurchaseCourseServiceImpl implements CoursePurchaseService {

    private final CourseMapper courseMapper;
    private final PurchasedCourseRepository purchasedCourseRepository;
    private final GetUserService getUserService;
    private final GetCourseService getCourseService;
    private final CoinsRefresher coinsRefresher;
    private final PurchasePreconditionValidator validator;

    public PurchaseCourseServiceImpl(CourseMapper courseMapper,
                                     PurchasedCourseRepository purchasedCourseRepository,
                                     GetUserService getUserService,
                                     GetCourseService getCourseService) {
        this.courseMapper = courseMapper;
        this.purchasedCourseRepository = purchasedCourseRepository;
        this.coinsRefresher = new CoinsRefresher();
        this.getUserService = getUserService;
        this.getCourseService = getCourseService;
        this.validator = new PurchasePreconditionValidator();
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CourseDto payForCourse(Long courseId, Long userId) {
        Course course = getCourseService.getCourseByIdOrElseThrow(courseId);
        User user = getUserService.getByIdOrElseThrow(userId);
        User author = course.getAuthor();
        validator.validate(course, user, author);

        if (purchasedCourseRepository.findByIdCourseIdAndIdUserId(userId, courseId).isPresent()) {
            log.warn("Course with id: {} has already been purchased", courseId);
            return courseMapper.toDto(course);
        }

        long price = course.getPrice();
        validator.validateSufficientFunds(user.getBalance().getCoins(), price);

        coinsRefresher.decrease(user.getBalance(), price);
        coinsRefresher.increase(author.getBalance(), price);

        var id = PurchasedCourseId.builder()
                .courseId(courseId)
                .userId(userId)
                .build();
        var purchasedCourse = new PurchasedCourse(id);
        purchasedCourseRepository.saveAndFlush(purchasedCourse);

        log.info("Course with id: {} has been purchased", courseId);
        return courseMapper.toDto(course);
    }
}
