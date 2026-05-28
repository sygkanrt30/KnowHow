package ru.tbank.knowhow.controller.moderation;

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
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.security.AttributeName;
import ru.tbank.knowhow.service.moderation.ModerationService;
import ru.tbank.knowhow.service.moderation.verdict.CourseVerdictService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ModerationController.class)
@Tag("integration-controller")
class ModerationControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CourseVerdictService courseVerdictService;

    @MockitoBean
    private ModerationService moderationService;

    @Value("${server.base-url.course}")
    private String baseUrl;

    private String getUrl() {
        return baseUrl + "/moderation";
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @DisplayName("approveCourse should return 200 when course is approved successfully")
    void shouldApproveCourseSuccessfully() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";

        doNothing().when(courseVerdictService).approveCourse(courseId, moderatorUsername);

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/approve", courseId)
                .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .body()
                .asString()
                .isEqualTo("Course approved");

        verify(courseVerdictService, times(1)).approveCourse(courseId, moderatorUsername);
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @DisplayName("approveCourse should return 404 when course not found")
    void shouldReturn404WhenCourseNotFoundForApprove() {
        Long nonExistentCourseId = 999L;
        String moderatorUsername = "moderator";

        doThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId))
                .when(courseVerdictService).approveCourse(nonExistentCourseId, moderatorUsername);

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/approve", nonExistentCourseId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(courseVerdictService, times(1)).approveCourse(nonExistentCourseId, moderatorUsername);
    }

    @Test
    @DisplayName("approveCourse should return 401 when user is not authenticated")
    void shouldReturn401WhenNotAuthenticatedForApprove() {
        Long courseId = 1L;

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/approve", courseId)
                .with(csrf()))
                .hasStatus(HttpStatus.UNAUTHORIZED);

        verify(courseVerdictService, never()).approveCourse(anyLong(), anyString());
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @DisplayName("rejectCourse should return 200 when course is rejected successfully")
    void shouldRejectCourseSuccessfully() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Course does not meet quality standards";


        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/reject", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reason": "%s"
                        }
                        """.formatted(rejectionReason))
                .with(csrf()))
                .hasStatus(HttpStatus.OK)
                .body()
                .asString()
                .isEqualTo("Course rejected");

        verify(courseVerdictService, times(1)).rejectCourse(courseId, moderatorUsername, rejectionReason);
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @DisplayName("rejectCourse should return 200 with rejection reason containing special characters")
    void shouldRejectCourseWithSpecialCharactersReason() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Reason with special chars: !@#$%^&*()";

        doNothing().when(courseVerdictService).rejectCourse(courseId, moderatorUsername, rejectionReason);

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/reject", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reason": "%s"
                        }
                        """.formatted(rejectionReason))
                .with(csrf()))
                .hasStatus(HttpStatus.OK);

        verify(courseVerdictService, times(1)).rejectCourse(courseId, moderatorUsername, rejectionReason);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @DisplayName("rejectCourse should return 400 when reason is blank")
    void shouldReturn400WhenReasonIsBlank() {
        Long courseId = 1L;

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/reject", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reason": ""
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseVerdictService, never()).rejectCourse(anyLong(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @DisplayName("rejectCourse should return 400 when reason is missing")
    void shouldReturn400WhenReasonIsMissing() {
        Long courseId = 1L;

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/reject", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseVerdictService, never()).rejectCourse(anyLong(), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "MODERATOR", username = "moderator")
    @DisplayName("rejectCourse should return 404 when course not found")
    void shouldReturn404WhenCourseNotFoundForReject() {
        Long nonExistentCourseId = 999L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Course not found";

        doThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId))
                .when(courseVerdictService).rejectCourse(nonExistentCourseId, moderatorUsername, rejectionReason);

        assertThat(mockMvc.post()
                .uri(getUrl() + "/{id}/reject", nonExistentCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "reason": "%s"
                        }
                        """.formatted(rejectionReason))
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(courseVerdictService, times(1)).rejectCourse(nonExistentCourseId, moderatorUsername, rejectionReason);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @DisplayName("onModeration should return 200 with list of courses on moderation")
    void shouldReturnListOfCoursesOnModeration() {
        Long moderatorId = 1L;

        CourseDto course1 = of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Course 1")
                .set(field(CourseDto::description), "Description 1")
                .set(field(CourseDto::price), 100L)
                .set(field(CourseDto::tags), new String[]{"java", "spring"})
                .set(field(CourseDto::rating), BigDecimal.valueOf(4.5))
                .set(field(CourseDto::authorId), 10L)
                .set(field(CourseDto::notForSale), false)
                .create();

        CourseDto course2 = of(CourseDto.class)
                .set(field(CourseDto::id), 2L)
                .set(field(CourseDto::title), "Course 2")
                .set(field(CourseDto::description), "Description 2")
                .set(field(CourseDto::price), 200L)
                .set(field(CourseDto::tags), new String[]{"python", "django"})
                .set(field(CourseDto::rating), BigDecimal.valueOf(4.8))
                .set(field(CourseDto::authorId), 20L)
                .set(field(CourseDto::notForSale), false)
                .create();

        List<CourseDto> expectedCourses = List.of(course1, course2);

        when(moderationService.findAllCoursesOnModerationByModeratorId(moderatorId))
                .thenReturn(expectedCourses);

        assertThat(mockMvc.get()
                .uri(getUrl() + "/queue/on_moderation")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), moderatorId))
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.length()").isEqualTo(2);
                    assertThat(json).extractingPath("$[0].id").isEqualTo(1);
                    assertThat(json).extractingPath("$[0].title").isEqualTo("Course 1");
                    assertThat(json).extractingPath("$[0].price").isEqualTo(100);
                    assertThat(json).extractingPath("$[0].tags.length()").isEqualTo(2);
                    assertThat(json).extractingPath("$[0].tags[0]").isEqualTo("java");
                    assertThat(json).extractingPath("$[0].tags[1]").isEqualTo("spring");
                    assertThat(json).extractingPath("$[0].rating").isEqualTo(4.5);
                    assertThat(json).extractingPath("$[1].id").isEqualTo(2);
                    assertThat(json).extractingPath("$[1].title").isEqualTo("Course 2");
                    assertThat(json).extractingPath("$[1].price").isEqualTo(200);
                });

        verify(moderationService, times(1)).findAllCoursesOnModerationByModeratorId(moderatorId);
    }

    @Test
    @WithMockUser(roles = "MODERATOR")
    @DisplayName("onModeration should return empty list when no courses on moderation")
    void shouldReturnEmptyListWhenNoCoursesOnModeration() {
        Long moderatorId = 1L;

        when(moderationService.findAllCoursesOnModerationByModeratorId(moderatorId))
                .thenReturn(List.of());

        assertThat(mockMvc.get()
                .uri(getUrl() + "/queue/on_moderation")
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), moderatorId))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .satisfies(json -> assertThat(json).extractingPath("$.length()").isEqualTo(0));

        verify(moderationService, times(1)).findAllCoursesOnModerationByModeratorId(moderatorId);
    }

    @Test
    @DisplayName("onModeration should return 401 when user is not authenticated")
    void shouldReturn401WhenNotAuthenticatedForOnModeration() {
        assertThat(mockMvc.get()
                .uri(getUrl() + "/queue/on_moderation")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.UNAUTHORIZED);

        verify(moderationService, never()).findAllCoursesOnModerationByModeratorId(anyLong());
    }
}