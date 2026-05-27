package ru.tbank.knowhow.service.course.purchase;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.ecxeption.AttemptPayForYourselfException;
import ru.tbank.knowhow.ecxeption.AttemptPayNotForSaleCourseException;
import ru.tbank.knowhow.ecxeption.InsufficientFundsException;
import ru.tbank.knowhow.model.*;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.PurchasedCourseRepository;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PurchaseCourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private PurchasedCourseRepository purchasedCourseRepository;

    @Mock
    private GetUserService getUserService;

    @Mock
    private GetCourseService getCourseService;

    @InjectMocks
    private PurchaseCourseServiceImpl purchaseCourseService;

    private User user;
    private User author;
    private Course course;
    private CourseDto expectedDto;

    private static final Long COURSE_ID = 1L;
    private static final Long USER_ID = 2L;
    private static final Long AUTHOR_ID = 3L;
    private static final Long COURSE_PRICE = 100L;
    private static final Long USER_BALANCE = 500L;
    private static final Long AUTHOR_BALANCE = 1000L;

    @BeforeEach
    void setUp() {
        user = createUser(USER_ID, USER_BALANCE);
        author = createUser(AUTHOR_ID, AUTHOR_BALANCE);
        course = createDefaultCourse();
        expectedDto = Instancio.create(CourseDto.class);
    }

    @Test
    void payForCourse_ShouldSuccessfullyPurchaseCourse_WhenAllConditionsMet() {
        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(user);
        when(purchasedCourseRepository.findByIdCourseIdAndIdUserId(USER_ID, COURSE_ID))
                .thenReturn(Optional.empty());
        when(courseMapper.toDto(course)).thenReturn(expectedDto);

        CourseDto result = purchaseCourseService.payForCourse(COURSE_ID, USER_ID);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(user.getBalance().getCoins()).isEqualTo(USER_BALANCE - COURSE_PRICE);
        assertThat(author.getBalance().getCoins()).isEqualTo(AUTHOR_BALANCE + COURSE_PRICE);

        verifyPurchasedCourseSaved();
    }

    @Test
    void payForCourse_ShouldReturnExistingCourseDto_WhenCourseAlreadyPurchased() {
        PurchasedCourse existingPurchasedCourse = Instancio.create(PurchasedCourse.class);

        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(user);
        when(purchasedCourseRepository.findByIdCourseIdAndIdUserId(USER_ID, COURSE_ID))
                .thenReturn(Optional.of(existingPurchasedCourse));
        when(courseMapper.toDto(course)).thenReturn(expectedDto);

        CourseDto result = purchaseCourseService.payForCourse(COURSE_ID, USER_ID);

        assertThat(result).isEqualTo(expectedDto);
        verify(purchasedCourseRepository, never()).saveAndFlush(any());
    }

    @Test
    void payForCourse_ShouldThrowException_WhenCourseIsNotForSale() {
        course.setNotForSale(true);

        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(user);

        assertThatThrownBy(() -> purchaseCourseService.payForCourse(COURSE_ID, USER_ID))
                .isInstanceOf(AttemptPayNotForSaleCourseException.class)
                .hasMessage("Course not for sale");

        verifyNoCoursePurchase();
    }

    @Test
    void payForCourse_ShouldThrowException_WhenCourseIsOnModeration() {
        course.setStatus(CourseStatus.ON_MODERATION);

        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(user);

        assertThatThrownBy(() -> purchaseCourseService.payForCourse(COURSE_ID, USER_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Can not pay for moderated courses!");

        verifyNoCoursePurchase();
    }

    @Test
    void payForCourse_ShouldThrowException_WhenUserIsAuthor() {
        course.setAuthor(user);

        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(user);

        assertThatThrownBy(() -> purchaseCourseService.payForCourse(COURSE_ID, USER_ID))
                .isInstanceOf(AttemptPayForYourselfException.class)
                .hasMessage("You can't pay for yourself!");

        verifyNoCoursePurchase();
    }

    @Test
    void payForCourse_ShouldThrowException_WhenInsufficientFunds() {
        Long lowBalance = 300L;
        User userWithLowBalance = createUser(USER_ID, lowBalance);
        course.getBusinessDetails().setPrice(500L);

        when(getCourseService.getCourseByIdOrElseThrow(COURSE_ID)).thenReturn(course);
        when(getUserService.getByIdOrElseThrow(USER_ID)).thenReturn(userWithLowBalance);
        when(purchasedCourseRepository.findByIdCourseIdAndIdUserId(USER_ID, COURSE_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseCourseService.payForCourse(COURSE_ID, USER_ID))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient funds!");

        verifyNoCoursePurchase();
    }


    private User createUser(Long id, Long balance) {
        return Instancio.of(User.class)
                .set(field(User::getId), id)
                .set(field(User::getBalance), createBalance(balance))
                .create();
    }

    private Balance createBalance(Long coins) {
        Balance balance = new Balance();
        balance.setCoins(coins);
        return balance;
    }

    private Course createDefaultCourse() {
        CourseBusinessDetails businessDetails = new CourseBusinessDetails();
        businessDetails.setPrice(COURSE_PRICE);
        return Instancio.of(Course.class)
                .set(field(Course::isNotForSale), false)
                .set(field(Course::getStatus), CourseStatus.PASSED_MODERATION)
                .set(field(Course::getBusinessDetails), businessDetails)
                .set(field(Course::getAuthor), author)
                .create();
    }

    private void verifyPurchasedCourseSaved() {
        ArgumentCaptor<PurchasedCourse> purchasedCourseCaptor = ArgumentCaptor.forClass(PurchasedCourse.class);
        verify(purchasedCourseRepository).saveAndFlush(purchasedCourseCaptor.capture());

        PurchasedCourse savedCourse = purchasedCourseCaptor.getValue();
        assertThat(savedCourse.getId().getCourseId()).isEqualTo(COURSE_ID);
        assertThat(savedCourse.getId().getUserId()).isEqualTo(USER_ID);
    }

    private void verifyNoCoursePurchase() {
        verify(purchasedCourseRepository, never()).saveAndFlush(any());
        verify(purchasedCourseRepository, never()).saveAndFlush(any());
    }
}