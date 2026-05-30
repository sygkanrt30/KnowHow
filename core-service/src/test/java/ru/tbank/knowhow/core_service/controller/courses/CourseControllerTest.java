package ru.tbank.knowhow.core_service.controller.courses;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import ru.tbank.knowhow.core_service.model.dto.course.request.CourseSearchRequest;
import ru.tbank.knowhow.core_service.model.dto.course.request.CreateCourseRequest;
import ru.tbank.knowhow.core_service.model.dto.course.request.UpdateCourseRequest;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.security.AttributeName;
import ru.tbank.knowhow.core_service.service.courses.CourseService;
import ru.tbank.knowhow.core_service.service.courses.UpdateCourseService;
import ru.tbank.knowhow.core_service.service.purchase.CoursePurchaseService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(CourseController.class)
@Tag("integration-controller")
class CourseControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private CoursePurchaseService purchaseCourseService;

    @MockitoBean
    private UpdateCourseService updateCourseService;

    @Value("${server.base-url.course}")
    private String url;

    @Test
    @WithMockUser
    @DisplayName("deleteCourse should return 200 when course is deleted successfully")
    void shouldDeleteCourseSuccessfully() {
        Long courseId = 1L;

        doNothing().when(courseService).deleteCourse(courseId);

        assertThat(mockMvc.delete()
                .uri(url + "/{id}", courseId)
                .with(csrf()))
                .hasStatusOk();

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    @Test
    @WithMockUser
    @DisplayName("deleteCourse should return 404 when course not found")
    void shouldReturn404WhenCourseNotFoundForDelete() {
        Long nonExistentCourseId = 999L;

        doThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId))
                .when(courseService).deleteCourse(nonExistentCourseId);

        assertThat(mockMvc.delete()
                .uri(url + "/{id}", nonExistentCourseId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);

        verify(courseService, times(1)).deleteCourse(nonExistentCourseId);
    }

    @Test
    @WithMockUser
    @DisplayName("deleteCourse should return 400 when course has been purchased")
    void shouldReturn400WhenCourseHasBeenPurchased() {
        Long courseId = 1L;

        doThrow(new IllegalArgumentException("Cannot delete course that has already been purchased"))
                .when(courseService).deleteCourse(courseId);

        assertThat(mockMvc.delete()
                .uri(url + "/{id}", courseId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseService, times(1)).deleteCourse(courseId);
    }

    @Test
    @WithMockUser(username = "author")
    @DisplayName("createCourse should return 201 when course is created successfully")
    void shouldCreateCourseSuccessfully() {
        String username = "author";
        CourseDto expectedCourse = of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Test Course")
                .set(field(CourseDto::description), "Test Description")
                .set(field(CourseDto::courseText), "Test Course Text")
                .set(field(CourseDto::price), 100L)
                .set(field(CourseDto::tags), new String[]{"java", "spring"})
                .set(field(CourseDto::rating), BigDecimal.valueOf(0.0))
                .set(field(CourseDto::authorId), 1L)
                .set(field(CourseDto::notForSale), false)
                .create();

        when(courseService.createCourse(any(CreateCourseRequest.class), eq(username)))
                .thenReturn(expectedCourse);

        assertThat(mockMvc.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Test Course",
                            "description": "Test Description",
                            "courseText": "Test Course Text",
                            "tags": ["java", "spring"]
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.CREATED)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(1);
                    assertThat(json).extractingPath("$.title").isEqualTo("Test Course");
                    assertThat(json).extractingPath("$.description").isEqualTo("Test Description");
                    assertThat(json).extractingPath("$.price").isEqualTo(100);
                    assertThat(json).extractingPath("$.tags.length()").isEqualTo(2);
                    assertThat(json).extractingPath("$.tags[0]").isEqualTo("java");
                    assertThat(json).extractingPath("$.tags[1]").isEqualTo("spring");
                });

        verify(courseService, times(1)).createCourse(any(CreateCourseRequest.class), eq(username));
    }

    @Test
    @WithMockUser(username = "author")
    @DisplayName("createCourse should return 400 when title is blank")
    void shouldReturn400WhenTitleIsBlank() {
        assertThat(mockMvc.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "",
                            "description": "Test Description",
                            "courseText": "Test Course Text",
                            "tags": ["java"]
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseService, never()).createCourse(any(), anyString());
    }

    @Test
    @WithMockUser(username = "author")
    @DisplayName("createCourse should return 400 when description is blank")
    void shouldReturn400WhenDescriptionIsBlank() {
        assertThat(mockMvc.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Test Course",
                            "description": "",
                            "courseText": "Test Course Text",
                            "tags": ["java"]
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseService, never()).createCourse(any(), anyString());
    }

    @Test
    @WithMockUser(username = "author")
    @DisplayName("createCourse should return 400 when courseText is blank")
    void shouldReturn400WhenCourseTextIsBlank() {
        assertThat(mockMvc.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Test Course",
                            "description": "Test Description",
                            "courseText": "",
                            "tags": ["java"]
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);

        verify(courseService, never()).createCourse(any(), anyString());
    }

    @Test
    @WithMockUser(username = "author")
    @DisplayName("createCourse should return 404 when author not found")
    void shouldReturn404WhenAuthorNotFound() {
        when(courseService.createCourse(any(CreateCourseRequest.class), anyString()))
                .thenThrow(new EntityNotFoundException("User not found with username: author"));

        assertThat(mockMvc.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Test Course",
                            "description": "Test Description",
                            "courseText": "Test Course Text",
                            "tags": ["java"]
                        }
                        """)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser
    @DisplayName("payForCourse should return 200 when payment is successful")
    void shouldPayForCourseSuccessfully() {
        Long courseId = 1L;
        Long userId = 1L;
        CourseDto expectedCourse = of(CourseDto.class)
                .set(field(CourseDto::id), courseId)
                .set(field(CourseDto::title), "Paid Course")
                .set(field(CourseDto::price), 500L)
                .create();

        when(purchaseCourseService.payForCourse(courseId, userId)).thenReturn(expectedCourse);

        assertThat(mockMvc.post()
                .uri(url + "/pay/{id}", courseId)
                .accept(MediaType.APPLICATION_JSON)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(courseId.intValue());
                    assertThat(json).extractingPath("$.title").isEqualTo("Paid Course");
                    assertThat(json).extractingPath("$.price").isEqualTo(500);
                });

        verify(purchaseCourseService, times(1)).payForCourse(courseId, userId);
    }

    @Test
    @WithMockUser
    @DisplayName("payForCourse should return 404 when course not found")
    void shouldReturn404WhenCourseNotFoundForPayment() {
        Long nonExistentCourseId = 999L;
        Long userId = 1L;

        when(purchaseCourseService.payForCourse(nonExistentCourseId, userId))
                .thenThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId));

        assertThat(mockMvc.post()
                .uri(url + "/pay/{id}", nonExistentCourseId)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser
    @DisplayName("payForCourse should return 400 when insufficient funds")
    void shouldReturn400WhenInsufficientFundsForPayment() {
        Long courseId = 1L;
        Long userId = 1L;

        when(purchaseCourseService.payForCourse(courseId, userId))
                .thenThrow(new IllegalArgumentException("Insufficient funds"));

        assertThat(mockMvc.post()
                .uri(url + "/pay/{id}", courseId)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser
    @DisplayName("retryPassModeration should return 200 when course is updated successfully")
    void shouldRetryPassModerationSuccessfully() {
        Long courseId = 1L;
        Long userId = 1L;
        CourseDto expectedCourse = of(CourseDto.class)
                .set(field(CourseDto::id), courseId)
                .set(field(CourseDto::title), "Updated Course")
                .set(field(CourseDto::description), "Updated Description")
                .set(field(CourseDto::courseText), "Updated Text")
                .set(field(CourseDto::tags), new String[]{"updated", "course"})
                .create();

        when(updateCourseService.updateCourse(any(UpdateCourseRequest.class), eq(courseId), eq(userId)))
                .thenReturn(expectedCourse);

        assertThat(mockMvc.put()
                .uri(url + "/retry-pass-moderation/{id}", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated Course",
                            "description": "Updated Description",
                            "courseText": "Updated Text",
                            "tags": ["updated", "course"]
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(courseId.intValue());
                    assertThat(json).extractingPath("$.title").isEqualTo("Updated Course");
                    assertThat(json).extractingPath("$.description").isEqualTo("Updated Description");
                    assertThat(json).extractingPath("$.tags.length()").isEqualTo(2);
                });

        verify(updateCourseService, times(1)).updateCourse(any(UpdateCourseRequest.class), eq(courseId), eq(userId));
    }

    @Test
    @WithMockUser
    @DisplayName("retryPassModeration should return 404 when course not found")
    void shouldReturn404WhenCourseNotFoundForUpdate() {
        Long nonExistentCourseId = 999L;
        Long userId = 1L;

        when(updateCourseService.updateCourse(any(UpdateCourseRequest.class), eq(nonExistentCourseId), eq(userId)))
                .thenThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId));

        assertThat(mockMvc.put()
                .uri(url + "/retry-pass-moderation/{id}", nonExistentCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "title": "Updated Course",
                            "description": "Updated Description",
                            "courseText": "Updated Text"
                        }
                        """)
                .requestAttr(AttributeName.USER_ID.getValue(), userId)
                .with(csrf()))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser
    @DisplayName("searchCourses should return page of courses")
    void shouldSearchCourses() {
        CourseDto course1 = of(CourseDto.class)
                .set(field(CourseDto::id), 1L)
                .set(field(CourseDto::title), "Java Course")
                .set(field(CourseDto::price), 100L)
                .create();

        CourseDto course2 = of(CourseDto.class)
                .set(field(CourseDto::id), 2L)
                .set(field(CourseDto::title), "Spring Course")
                .set(field(CourseDto::price), 200L)
                .create();

        Page<CourseDto> expectedPage = new PageImpl<>(List.of(course1, course2));

        when(courseService.searchCourses(any(CourseSearchRequest.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        assertThat(mockMvc.get()
                .uri(url + "/search?title=Java&minPrice=0&maxPrice=500")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.content.length()").isEqualTo(2);
                    assertThat(json).extractingPath("$.content[0].id").isEqualTo(1);
                    assertThat(json).extractingPath("$.content[0].title").isEqualTo("Java Course");
                    assertThat(json).extractingPath("$.content[1].id").isEqualTo(2);
                    assertThat(json).extractingPath("$.content[1].title").isEqualTo("Spring Course");
                    assertThat(json).extractingPath("$.page.totalElements").isEqualTo(2);
                });

        verify(courseService, times(1)).searchCourses(any(CourseSearchRequest.class), any(Pageable.class));
    }

    @Test
    @WithMockUser
    @DisplayName("searchCourses should return empty page when no courses found")
    void shouldReturnEmptyPageWhenNoCoursesFound() {
        Page<CourseDto> emptyPage = Page.empty();

        when(courseService.searchCourses(any(CourseSearchRequest.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        assertThat(mockMvc.get()
                .uri(url + "/search?title=NonExistent")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatusOk()
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.content.length()").isEqualTo(0);
                    assertThat(json).extractingPath("$.page.totalElements").isEqualTo(0);
                });
    }

    @Test
    @WithMockUser
    @DisplayName("searchCourses should handle tags parameter")
    void shouldSearchCoursesWithTags() {
        Page<CourseDto> expectedPage = new PageImpl<>(List.of());

        when(courseService.searchCourses(any(CourseSearchRequest.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        assertThat(mockMvc.get()
                .uri(url + "/search?tags=java,spring&tags=python")
                .accept(MediaType.APPLICATION_JSON))
                .hasStatusOk();

        verify(courseService, times(1)).searchCourses(any(CourseSearchRequest.class), any(Pageable.class));
    }

    @Test
    @WithMockUser
    @DisplayName("getCourse should return 200 with course")
    void shouldGetCourseById() {
        Long courseId = 1L;
        CourseDto expectedCourse = of(CourseDto.class)
                .set(field(CourseDto::id), courseId)
                .set(field(CourseDto::title), "Test Course")
                .set(field(CourseDto::description), "Test Description")
                .set(field(CourseDto::price), 100L)
                .create();

        when(courseService.getCourseDtoByIdOrElseThrow(courseId)).thenReturn(expectedCourse);

        assertThat(mockMvc.get()
                .uri(url + "/{id}", courseId)
                .accept(MediaType.APPLICATION_JSON))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .satisfies(json -> {
                    assertThat(json).extractingPath("$.id").isEqualTo(courseId.intValue());
                    assertThat(json).extractingPath("$.title").isEqualTo("Test Course");
                    assertThat(json).extractingPath("$.description").isEqualTo("Test Description");
                    assertThat(json).extractingPath("$.price").isEqualTo(100);
                });

        verify(courseService, times(1)).getCourseDtoByIdOrElseThrow(courseId);
    }

    @Test
    @WithMockUser
    @DisplayName("getCourse should return 404 when course not found")
    void shouldReturn404WhenCourseNotFound() {
        Long nonExistentCourseId = 999L;

        when(courseService.getCourseDtoByIdOrElseThrow(nonExistentCourseId))
                .thenThrow(new EntityNotFoundException("Course not found with id: " + nonExistentCourseId));

        assertThat(mockMvc.get()
                .uri(url + "/{id}", nonExistentCourseId)
                .accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}