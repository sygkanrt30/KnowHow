package ru.tbank.knowhow.service.rating;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.repository.RatingRepository;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.course.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class RatingServiceImplTest {

    @Mock
    private PurchasedCourseService purchasedCourseService;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private GetUserService getUserService;

    @Mock
    private GetCourseService getCourseService;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private final Random random = new Random();
    private Long courseId;
    private Long userId;

    @BeforeEach
    void setUp() {
        courseId = random.nextLong();
        userId = random.nextLong();
    }

    @Test
    void insertRating_ShouldUpdateExistingRating_WhenRatingExists() {
        Integer newGrade = 4;

        var existingRating = Rating.builder()
                .id(10L)
                .grade((short) 3)
                .build();

        when(ratingRepository.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.of(existingRating));

        ratingService.insertRating(courseId, userId, newGrade);

        assertThat(existingRating.getGrade()).isEqualTo((short) 4);
        verify(ratingRepository, never()).save(any(Rating.class));
        verify(getCourseService, never()).getCourseByIdOrElseThrow(courseId);
        verify(getUserService, never()).getByIdOrElseThrow(userId);
        verify(purchasedCourseService, never()).existsPurchasedCourse(courseId, userId);
    }

    @Test
    void insertRating_ShouldSaveNewRating_WhenRatingDoesNotExistAndCoursePurchased() {
        Integer grade = 5;
        Course course = createCourseWithId(courseId);
        User user = createUserWithId(userId);

        when(ratingRepository.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.empty());
        when(getCourseService.getCourseByIdOrElseThrow(courseId))
                .thenReturn(course);
        when(getUserService.getByIdOrElseThrow(userId))
                .thenReturn(user);
        when(purchasedCourseService.existsPurchasedCourse(courseId, userId))
                .thenReturn(true);

        ratingService.insertRating(courseId, userId, grade);

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingCaptor.capture());

        Rating savedRating = ratingCaptor.getValue();
        assertThat(savedRating.getGrade()).isEqualTo((short) 5);
        assertThat(savedRating.getCourse()).isEqualTo(course);
        assertThat(savedRating.getUser()).isEqualTo(user);
    }

    @Test
    void insertRating_ShouldThrowException_WhenCourseNotPurchased() {
        Integer grade = 3;
        User user = createUserWithId(userId);

        when(ratingRepository.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.empty());
        when(getCourseService.getCourseByIdOrElseThrow(courseId))
                .thenReturn(new Course());
        when(getUserService.getByIdOrElseThrow(userId))
                .thenReturn(user);
        when(purchasedCourseService.existsPurchasedCourse(courseId, userId))
                .thenReturn(false);

        assertThatThrownBy(() -> ratingService.insertRating(courseId, userId, grade))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You can not rate course without purchasing");

        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void insertRating_ShouldSaveWithGrade1_WhenGradeIsMinimum() {
        Integer grade = 1;
        Course course = createCourseWithId(courseId);
        User user = createUserWithId(userId);

        when(ratingRepository.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.empty());
        when(getCourseService.getCourseByIdOrElseThrow(courseId))
                .thenReturn(course);
        when(getUserService.getByIdOrElseThrow(userId))
                .thenReturn(user);
        when(purchasedCourseService.existsPurchasedCourse(courseId, userId))
                .thenReturn(true);

        ratingService.insertRating(courseId, userId, grade);

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingCaptor.capture());

        assertThat(ratingCaptor.getValue().getGrade()).isEqualTo((short) 1);
    }

    @Test
    void insertRating_ShouldSaveWithGrade5_WhenGradeIsMaximum() {
        Integer grade = 5;
        Course course = createCourseWithId(courseId);
        User user = createUserWithId(userId);

        when(ratingRepository.findByCourseIdAndUserId(courseId, userId))
                .thenReturn(Optional.empty());
        when(getCourseService.getCourseByIdOrElseThrow(courseId))
                .thenReturn(course);
        when(getUserService.getByIdOrElseThrow(userId))
                .thenReturn(user);
        when(purchasedCourseService.existsPurchasedCourse(courseId, userId))
                .thenReturn(true);

        ratingService.insertRating(courseId, userId, grade);

        ArgumentCaptor<Rating> ratingCaptor = ArgumentCaptor.forClass(Rating.class);
        verify(ratingRepository).save(ratingCaptor.capture());

        assertThat(ratingCaptor.getValue().getGrade()).isEqualTo((short) 5);
    }

    private Course createCourseWithId(Long courseId) {
        var course = new Course();
        course.setId(courseId);
        return course;
    }

    private User createUserWithId(Long userId) {
        var user = new User();
        user.setId(userId);
        return user;
    }
}