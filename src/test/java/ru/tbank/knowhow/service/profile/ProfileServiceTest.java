package ru.tbank.knowhow.service.profile;

import jakarta.persistence.EntityNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.dto.response.*;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.model.mapper.RatingMapper;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.GetUserInfoService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private GetUserInfoService getUserInfoService;

    @Mock
    private GetCourseService getCourseService;

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfile_ShouldReturnModeratorProfile_WhenUserIsModerator() {
        Long userId = 1L;
        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("moderator");
        when(user.getEmail()).thenReturn("moderator@test.com");
        when(user.getRole()).thenReturn(Role.MODERATOR);
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo("moderator");
        assertThat(result.email()).isEqualTo("moderator@test.com");
        assertThat(result.balance()).isNull();
        assertThat(result.purchasedCourses()).isNull();
        assertThat(result.givenGrades()).isNull();

        verify(getCourseService, never()).findAllPurchasedCourses(any());
        verify(balanceMapper, never()).toDto(any(), any());
        verify(ratingMapper, never()).toDto(any());
    }

    @Test
    void getProfile_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        Long userId = 999L;
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getProfile_ShouldReturnFullProfile_WhenUserIsNotModerator() {
        Long userId = 1L;

        Balance balance = Instancio.of(Balance.class)
                .set(field(Balance::getCoins), 1000L)
                .create();

        Rating rating1 = Instancio.of(Rating.class)
                .set(field(Rating::getId), 1L)
                .set(field(Rating::getGrade), (short) 5)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-15T10:00:00Z"))
                .create();

        Rating rating2 = Instancio.of(Rating.class)
                .set(field(Rating::getId), 2L)
                .set(field(Rating::getGrade), (short) 4)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-20T10:00:00Z"))
                .create();

        List<Rating> ratings = List.of(rating1, rating2);

        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("user");
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getBalance()).thenReturn(balance);
        when(user.getUserRatings()).thenReturn(ratings);
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));

        BalanceDto balanceDto = Instancio.create(BalanceDto.class);
        when(balanceMapper.toDto(balance, userId)).thenReturn(balanceDto);

        CourseDto course1 = Instancio.of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Course 1")
                .set(field(CourseDto::price), 100L)
                .create();

        CourseDto course2 = Instancio.of(CourseDto.class)
                .set(field(CourseDto::id), 2L)
                .set(field(CourseDto::title), "Course 2")
                .set(field(CourseDto::price), 200L)
                .create();

        List<CourseDto> courses = List.of(course1, course2);
        when(getCourseService.findAllPurchasedCourses(userId)).thenReturn(courses);

        RatingDto ratingDto1 = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 1L)
                .set(field(RatingDto::grade), (short) 5)
                .set(field(RatingDto::userId), userId)
                .create();

        RatingDto ratingDto2 = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 2L)
                .set(field(RatingDto::grade), (short) 4)
                .set(field(RatingDto::userId), userId)
                .create();

        when(ratingMapper.toDto(rating1)).thenReturn(ratingDto1);
        when(ratingMapper.toDto(rating2)).thenReturn(ratingDto2);

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo("user");
        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.balance()).isEqualTo(balanceDto);
        assertThat(result.purchasedCourses()).hasSize(2);
        assertThat(result.purchasedCourses()).containsExactlyElementsOf(courses);
        assertThat(result.countOfPurchasedCourses()).isEqualTo(2);
        assertThat(result.givenGrades()).hasSize(2);
        assertThat(result.countOfGivenGrades()).isEqualTo(2);
    }

    @Test
    void getProfile_ShouldSortRatingsByCreatedAtDescending() {
        Long userId = 1L;
        Balance balance = Instancio.create(Balance.class);

        Rating ratingOld = Instancio.of(Rating.class)
                .set(field(Rating::getId), 1L)
                .set(field(Rating::getGrade), (short) 3)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-01T10:00:00Z"))
                .create();

        Rating ratingMiddle = Instancio.of(Rating.class)
                .set(field(Rating::getId), 2L)
                .set(field(Rating::getGrade), (short) 4)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-10T10:00:00Z"))
                .create();

        Rating ratingNew = Instancio.of(Rating.class)
                .set(field(Rating::getId), 3L)
                .set(field(Rating::getGrade), (short) 5)
                .set(field(Rating::getCreatedAt), Instant.parse("2024-01-20T10:00:00Z"))
                .create();

        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("user");
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getBalance()).thenReturn(balance);
        when(user.getUserRatings()).thenReturn(List.of(ratingOld, ratingMiddle, ratingNew));
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));

        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(getCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        RatingDto ratingDtoOld = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 1L)
                .set(field(RatingDto::grade), (short) 3)
                .create();

        RatingDto ratingDtoMiddle = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 2L)
                .set(field(RatingDto::grade), (short) 4)
                .create();

        RatingDto ratingDtoNew = Instancio.of(RatingDto.class)
                .set(field(RatingDto::id), 3L)
                .set(field(RatingDto::grade), (short) 5)
                .create();

        when(ratingMapper.toDto(ratingOld)).thenReturn(ratingDtoOld);
        when(ratingMapper.toDto(ratingMiddle)).thenReturn(ratingDtoMiddle);
        when(ratingMapper.toDto(ratingNew)).thenReturn(ratingDtoNew);

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.givenGrades()).containsExactly(ratingDtoNew, ratingDtoMiddle, ratingDtoOld);
    }

    @Test
    void getProfile_ShouldHandleEmptyPurchasedCourses() {
        Long userId = 1L;
        Balance balance = Instancio.create(Balance.class);

        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("user");
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getBalance()).thenReturn(balance);
        when(user.getUserRatings()).thenReturn(List.of());
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));

        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(getCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.purchasedCourses()).isEmpty();
        assertThat(result.countOfPurchasedCourses()).isZero();
        assertThat(result.givenGrades()).isEmpty();
        assertThat(result.countOfGivenGrades()).isZero();
    }

    @Test
    void getProfile_ShouldHandleUserRole() {
        Long userId = 1L;
        Balance balance = Instancio.create(Balance.class);

        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("user");
        when(user.getEmail()).thenReturn("user@test.com");
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getBalance()).thenReturn(balance);
        when(user.getUserRatings()).thenReturn(List.of());
        when(getUserInfoService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));

        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(getCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.balance()).isNotNull();
        verify(getCourseService).findAllPurchasedCourses(userId);
    }
}