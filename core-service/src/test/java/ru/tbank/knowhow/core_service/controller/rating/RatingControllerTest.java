package ru.tbank.knowhow.core_service.controller.rating;


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
import ru.tbank.knowhow.core_service.security.AttributeName;
import ru.tbank.knowhow.core_service.service.rating.RatingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RatingController.class)
@Tag("integration-controller")
class RatingControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @Value("${server.base-url.course}")
    private String url;

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 200 if rating is inserted successfully")
    void shouldReturn200WhenRatingInsertedSuccessfully() {
        Long courseId = 1L;
        Long userId = 2L;
        int grade = 3;

        doNothing().when(ratingService).insertRating(courseId, userId, grade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", Integer.toString(grade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON);

        verify(ratingService).insertRating(courseId, userId, grade);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 200 when rating is updated (already exists)")
    void shouldReturn200WhenRatingUpdated() {
        Long courseId = 1L;
        Long userId = 2L;
        int grade = 5;

        doNothing().when(ratingService).insertRating(courseId, userId, grade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", Integer.toString(grade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatusOk();

        verify(ratingService).insertRating(courseId, userId, grade);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 400 when grade is greater than 5")
    void shouldReturn400WhenGradeIsGreaterThan5() {
        Long courseId = 1L;
        Long userId = 2L;
        int invalidGrade = 6;

        doThrow(new IllegalArgumentException("Grade must be between 1 and 5"))
                .when(ratingService).insertRating(courseId, userId, invalidGrade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", Integer.toString(invalidGrade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 404 when course not found")
    void shouldReturn404WhenCourseNotFound() {
        Long nonExistentCourseId = 999L;
        Long userId = 2L;
        int grade = 3;

        doThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId))
                .when(ratingService).insertRating(nonExistentCourseId, userId, grade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", nonExistentCourseId)
                .param("grade", Integer.toString(grade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 404 when user not found")
    void shouldReturn404WhenUserNotFound() {
        Long courseId = 1L;
        Long nonExistentUserId = 999L;
        int grade = 3;

        doThrow(new EntityNotFoundException("User not found by id: " + nonExistentUserId))
                .when(ratingService).insertRating(courseId, nonExistentUserId, grade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", Integer.toString(grade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), nonExistentUserId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 400 when user hasn't purchased the course")
    void shouldReturn400WhenCourseNotPurchased() {
        Long courseId = 1L;
        Long userId = 2L;
        int grade = 3;

        doThrow(new IllegalArgumentException("You can not rate course without purchasing"))
                .when(ratingService).insertRating(courseId, userId, grade);

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", Integer.toString(grade))
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 400 when grade parameter is missing")
    void shouldReturn400WhenGradeParameterMissing() {
        Long courseId = 1L;
        Long userId = 2L;

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(ratingService, never()).insertRating(any(), any(), any());
    }

    @Test
    @WithMockUser
    @DisplayName("insertRating should return 400 when grade parameter is not a number")
    void shouldReturn400WhenGradeIsNotNumber() {
        Long courseId = 1L;
        Long userId = 2L;

        assertThat(mockMvc.post()
                .uri(url + "/{courseId}/rating", courseId)
                .param("grade", "invalid_number")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(ratingService, never()).insertRating(any(), any(), any());
    }
}