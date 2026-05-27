package ru.tbank.knowhow.service.profile;

import jakarta.persistence.EntityNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.model.Balance;
import ru.tbank.knowhow.model.Rating;
import ru.tbank.knowhow.model.Role;
import ru.tbank.knowhow.model.UserContact;
import ru.tbank.knowhow.model.dto.response.*;
import ru.tbank.knowhow.model.mapper.BalanceMapper;
import ru.tbank.knowhow.model.mapper.RatingMapper;
import ru.tbank.knowhow.service.course.purchased.PurchasedCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ProfileServiceTest {

    @Mock
    private GetUserService getUserService;

    @Mock
    private PurchasedCourseService purchasedCourseService;

    @Mock
    private BalanceMapper balanceMapper;

    @Mock
    private RatingMapper ratingMapper;

    @InjectMocks
    private ProfileService profileService;

    private static final String USERNAME = "user";
    private static final String EMAIL = "user@test.com";

    @Test
    void getProfile_ShouldReturnModeratorProfile_WhenUserIsModerator() {
        Long userId = 1L;
        String username = "moderator";
        String email = "moderator@test.com";
        mockUserProjectionForModerator(userId,  username, email);

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.balance()).isNull();
        assertThat(result.purchasedCourses()).isNull();
        assertThat(result.givenGrades()).isNull();

        verify(purchasedCourseService, never()).findAllPurchasedCourses(any());
        verify(balanceMapper, never()).toDto(any(), any());
        verify(ratingMapper, never()).toDto(any());
    }


    @Test
    void getProfile_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        Long userId = 999L;

        when(getUserService.getProjectionForProfile(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getProfile(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void getProfile_ShouldReturnFullProfile_WhenUserIsNotModerator() {
        Long userId = 1L;

        Balance balance = createBalance();
        BalanceDto balanceDto = Instancio.create(BalanceDto.class);
        when(balanceMapper.toDto(balance, userId)).thenReturn(balanceDto);

        List<Rating> ratings = ProfileTestDataFabric.createRatings();
        List<RatingDto> ratingDtos = ProfileTestDataFabric.createRatingDtos();
        for (int i = 0; i < ratings.size(); i++) {
            when(ratingMapper.toDto(ratings.get(i))).thenReturn(ratingDtos.get(i));
        }

        mockUserProjection(userId, balance, ratings);

        List<CourseDto> courses = ProfileTestDataFabric.createCourseDtos();
        when(purchasedCourseService.findAllPurchasedCourses(userId)).thenReturn(courses);

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo(USERNAME);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.balance()).isEqualTo(balanceDto);
        assertThat(result.purchasedCourses()).hasSize(2);
        assertThat(result.purchasedCourses()).containsExactlyElementsOf(courses);
        assertThat(result.countOfPurchasedCourses()).isEqualTo(2);
        assertThat(result.givenGrades()).hasSize(3);
        assertThat(result.countOfGivenGrades()).isEqualTo(3);
    }

    @Test
    void getProfile_ShouldSortRatingsByCreatedAtDescending() {
        Long userId = 1L;
        Balance balance = createBalance();

        List<Rating> ratings = ProfileTestDataFabric.createRatings();
        List<RatingDto> ratingDtos = ProfileTestDataFabric.createRatingDtos();
        for (int i = 0; i < ratings.size(); i++) {
            when(ratingMapper.toDto(ratings.get(i))).thenReturn(ratingDtos.get(i));
        }

        mockUserProjection(userId, balance, ratings);
        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(purchasedCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.givenGrades()).containsExactly(
                ratingDtos.get(2),
                ratingDtos.get(1),
                ratingDtos.get(0)
        );
    }

    @Test
    void getProfile_ShouldHandleEmptyPurchasedCourses() {
        Long userId = 1L;
        Balance balance = createBalance();

        mockUserProjection(userId, balance, List.of());
        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(purchasedCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.purchasedCourses()).isEmpty();
        assertThat(result.countOfPurchasedCourses()).isZero();
        assertThat(result.givenGrades()).isEmpty();
        assertThat(result.countOfGivenGrades()).isZero();
    }

    @Test
    void getProfile_ShouldHandleUserRole() {
        Long userId = 1L;
        Balance balance = createBalance();

        mockUserProjection(userId, balance, List.of());
        when(balanceMapper.toDto(balance, userId)).thenReturn(Instancio.create(BalanceDto.class));
        when(purchasedCourseService.findAllPurchasedCourses(userId)).thenReturn(List.of());

        ProfileDto result = profileService.getProfile(userId);

        assertThat(result.balance()).isNotNull();
        verify(purchasedCourseService).findAllPurchasedCourses(userId);
    }

    private void mockUserProjection(Long userId, Balance balance, List<Rating> of) {
        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        UserContact userContact = new UserContact(EMAIL);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn(USERNAME);
        when(user.getUserContact()).thenReturn(userContact);
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getBalance()).thenReturn(balance);
        when(user.getUserRatings()).thenReturn(of);
        when(getUserService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));
    }

    private Balance createBalance() {
        return Instancio.of(Balance.class)
                .set(field(Balance::getCoins), 1000L)
                .create();
    }

    private void mockUserProjectionForModerator(Long userId, String username, String email) {
        UserProjectionForProfile user = mock(UserProjectionForProfile.class);
        UserContact userContact = new UserContact(email);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn(username);
        when(user.getUserContact()).thenReturn(userContact);
        when(user.getRole()).thenReturn(Role.MODERATOR);
        when(getUserService.getProjectionForProfile(userId)).thenReturn(Optional.of(user));
    }
}