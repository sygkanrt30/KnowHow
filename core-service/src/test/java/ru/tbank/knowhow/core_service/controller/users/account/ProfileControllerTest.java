package ru.tbank.knowhow.core_service.controller.users.account;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.model.dto.user.profile.response.ProfileDto;
import ru.tbank.knowhow.core_service.model.dto.rating.response.RatingDto;
import ru.tbank.knowhow.core_service.security.AttributeName;
import ru.tbank.knowhow.core_service.service.users.profile.GetProfileService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebMvcTest(ProfileController.class)
@Tag("integration-controller")
class ProfileControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private GetProfileService getProfileService;

    @Value("${server.base-url.profile}")
    private String url;

    @Test
    @WithMockUser
    @DisplayName("getProfile should return 200 with full profile for regular user")
    void shouldReturnFullProfileForRegularUser() {
        Long userId = 1L;

        BalanceDto balanceDto = of(BalanceDto.class)
                .set(field(BalanceDto::id), 1L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 1500L)
                .create();

        CourseDto course1 = of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Spring Boot Course")
                .create();

        CourseDto course2 = of(CourseDto.class)
                .set(field(CourseDto::id), 2L)
                .set(field(CourseDto::title), "Java Advanced")
                .create();

        RatingDto rating1 = of(RatingDto.class)
                .set(field(RatingDto::id), 1L)
                .set(field(RatingDto::grade), 5)
                .set(field(RatingDto::userId), userId)
                .set(field(RatingDto::course), course1)
                .create();

        RatingDto rating2 = of(RatingDto.class)
                .set(field(RatingDto::id), 2L)
                .set(field(RatingDto::grade), 4)
                .set(field(RatingDto::userId), userId)
                .set(field(RatingDto::course), course2)
                .create();

        ProfileDto expectedProfile = of(ProfileDto.class)
                .set(field(ProfileDto::id), userId)
                .set(field(ProfileDto::username), "john_doe")
                .set(field(ProfileDto::email), "john@example.com")
                .set(field(ProfileDto::balance), balanceDto)
                .set(field(ProfileDto::purchasedCourses), List.of(course1, course2))
                .set(field(ProfileDto::countOfPurchasedCourses), 2)
                .set(field(ProfileDto::givenGrades), List.of(rating1, rating2))
                .set(field(ProfileDto::countOfGivenGrades), 2)
                .create();

        when(getProfileService.getProfile(userId)).thenReturn(expectedProfile);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.username").isEqualTo("john_doe");
                    assertThat(json).extractingPath("$.email").isEqualTo("john@example.com");
                    assertThat(json).extractingPath("$.balance.id").isEqualTo(1);
                    assertThat(json).extractingPath("$.balance.userId").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.balance.coins").isEqualTo(1500);
                    assertThat(json).extractingPath("$.countOfPurchasedCourses").isEqualTo(2);
                    assertThat(json).extractingPath("$.purchasedCourses.length()").isEqualTo(2);
                    assertThat(json).extractingPath("$.purchasedCourses[0].title").isEqualTo("Spring Boot Course");
                    assertThat(json).extractingPath("$.countOfGivenGrades").isEqualTo(2);
                    assertThat(json).extractingPath("$.givenGrades[0].grade").isEqualTo(5);
                    assertThat(json).extractingPath("$.givenGrades[0].userId").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.givenGrades[0].course.id").isEqualTo(1);
                    assertThat(json).extractingPath("$.givenGrades[0].course.title").isEqualTo("Spring Boot Course");
                });

        verify(getProfileService, times(1)).getProfile(userId);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @DisplayName("getProfile should return limited profile for moderator")
    void shouldReturnLimitedProfileForModerator() {
        Long userId = 2L;

        ProfileDto expectedProfile = of(ProfileDto.class)
                .set(field(ProfileDto::id), userId)
                .set(field(ProfileDto::username), "moderator_user")
                .set(field(ProfileDto::email), "moderator@example.com")
                .set(field(ProfileDto::balance), null)
                .set(field(ProfileDto::purchasedCourses), null)
                .set(field(ProfileDto::countOfPurchasedCourses), null)
                .set(field(ProfileDto::givenGrades), null)
                .set(field(ProfileDto::countOfGivenGrades), null)
                .create();

        when(getProfileService.getProfile(userId)).thenReturn(expectedProfile);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.username").isEqualTo("moderator_user");
                    assertThat(json).extractingPath("$.email").isEqualTo("moderator@example.com");
                });

        verify(getProfileService, times(1)).getProfile(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("getProfile should return profile with empty lists when user has no purchases")
    void shouldReturnProfileWithEmptyListsWhenNoPurchases() {
        Long userId = 3L;

        BalanceDto balanceDto = of(BalanceDto.class)
                .set(field(BalanceDto::id), 1L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 0L)
                .create();

        ProfileDto expectedProfile = of(ProfileDto.class)
                .set(field(ProfileDto::id), userId)
                .set(field(ProfileDto::username), "new_user")
                .set(field(ProfileDto::email), "new@example.com")
                .set(field(ProfileDto::balance), balanceDto)
                .set(field(ProfileDto::purchasedCourses), List.of())
                .set(field(ProfileDto::countOfPurchasedCourses), 0)
                .set(field(ProfileDto::givenGrades), List.of())
                .set(field(ProfileDto::countOfGivenGrades), 0)
                .create();

        when(getProfileService.getProfile(userId)).thenReturn(expectedProfile);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.countOfPurchasedCourses").isEqualTo(0);
                    assertThat(json).extractingPath("$.purchasedCourses.length()").isEqualTo(0);
                    assertThat(json).extractingPath("$.countOfGivenGrades").isEqualTo(0);
                    assertThat(json).extractingPath("$.givenGrades.length()").isEqualTo(0);
                });

        verify(getProfileService, times(1)).getProfile(userId);
    }

    @Test
    @WithMockUser
    @DisplayName("getProfile should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
        Long nonExistentUserId = 999L;

        when(getProfileService.getProfile(nonExistentUserId))
                .thenThrow(new EntityNotFoundException("User not found"));

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), nonExistentUserId))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(getProfileService, times(1)).getProfile(nonExistentUserId);
    }

    @Test
    @DisplayName("getProfile should return 401 when user is not authenticated")
    void shouldReturn401WhenUserNotAuthenticated() {
        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.UNAUTHORIZED);

        verify(getProfileService, never()).getProfile(anyLong());
    }

    @Test
    @WithMockUser
    @DisplayName("getProfile should return profile with user ratings")
    void shouldReturnProfileWithRatings() {
        Long userId = 4L;

        CourseDto course1 = of(CourseDto.class)
                .set(field(CourseDto::id), 10L)
                .set(field(CourseDto::title), "Spring Course")
                .create();

        RatingDto rating1 = of(RatingDto.class)
                .set(field(RatingDto::id), 1L)
                .set(field(RatingDto::grade), 5)
                .set(field(RatingDto::userId), userId)
                .set(field(RatingDto::course), course1)
                .create();

        ProfileDto expectedProfile = of(ProfileDto.class)
                .set(field(ProfileDto::id), userId)
                .set(field(ProfileDto::username), "rated_user")
                .set(field(ProfileDto::email), "rated@example.com")
                .set(field(ProfileDto::givenGrades), List.of(rating1))
                .set(field(ProfileDto::countOfGivenGrades), 1)
                .create();

        when(getProfileService.getProfile(userId)).thenReturn(expectedProfile);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.givenGrades.length()").isEqualTo(1);
                    assertThat(json).extractingPath("$.givenGrades[0].grade").isEqualTo(5);
                    assertThat(json).extractingPath("$.givenGrades[0].userId").isEqualTo(userId.intValue());
                    assertThat(json).extractingPath("$.givenGrades[0].course.id").isEqualTo(10);
                    assertThat(json).extractingPath("$.givenGrades[0].course.title").isEqualTo("Spring Course");
                });

        verify(getProfileService, times(1)).getProfile(userId);
    }
    
    @Test
    @WithMockUser
    @DisplayName("getProfile should handle user with zero balance")
    void shouldReturnProfileWithZeroBalance() {
        Long userId = 6L;

        BalanceDto zeroBalance = of(BalanceDto.class)
                .set(field(BalanceDto::id), 1L)
                .set(field(BalanceDto::userId), userId)
                .set(field(BalanceDto::coins), 0L)
                .create();

        ProfileDto expectedProfile = of(ProfileDto.class)
                .set(field(ProfileDto::id), userId)
                .set(field(ProfileDto::username), "poor_user")
                .set(field(ProfileDto::email), "poor@example.com")
                .set(field(ProfileDto::balance), zeroBalance)
                .set(field(ProfileDto::purchasedCourses), List.of())
                .set(field(ProfileDto::countOfPurchasedCourses), 0)
                .create();

        when(getProfileService.getProfile(userId)).thenReturn(expectedProfile);

        assertThat(mockMvc.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.balance.coins").isEqualTo(0);
                    assertThat(json).extractingPath("$.balance.userId").isEqualTo(userId.intValue());
                });

        verify(getProfileService, times(1)).getProfile(userId);
    }
}