package ru.tbank.knowhow.core_service.service.courses;

import jakarta.persistence.EntityNotFoundException;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.course.request.CourseSearchRequest;
import ru.tbank.knowhow.core_service.model.dto.course.request.CreateCourseRequest;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.mappers.CourseMapper;
import ru.tbank.knowhow.core_service.repository.CourseRepository;
import ru.tbank.knowhow.core_service.service.event.NotificationEventPublisher;
import ru.tbank.knowhow.core_service.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.core_service.service.moderation.ModeratorManager;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private GetUserService getUserService;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ModeratorManager moderatorManager;

    @Mock
    private PurchasedCourseService purchasedCourseService;

    @Mock
    private Pageable pageable;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(
                courseRepository,
                getUserService,
                courseMapper,
                purchasedCourseService,
                mock(NotificationEventPublisher.class),
                moderatorManager,
                20
        );
    }

    @Test
    void deleteCourse_ShouldDeleteCourse_WhenCourseExistsAndNotPurchased() {
        Long courseId = 1L;
        Course course = Instancio.create(Course.class);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(purchasedCourseService.existsPurchasedCourseByCourseId(courseId)).thenReturn(false);

        courseService.deleteCourse(courseId);

        verify(courseRepository).delete(course);
    }

    @Test
    void deleteCourse_ShouldThrowEntityNotFoundException_WhenCourseDoesNotExist() {
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse(courseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course not found with id: " + courseId);

        verify(courseRepository, never()).delete(any(Course.class));
    }

    @Test
    void deleteCourse_ShouldThrowIllegalStateException_WhenCourseAlreadyPurchased() {
        Long courseId = 1L;
        Course course = Instancio.create(Course.class);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(purchasedCourseService.existsPurchasedCourseByCourseId(courseId)).thenReturn(true);

        assertThatThrownBy(() -> courseService.deleteCourse(courseId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot delete course that has already been purchased");

        verify(courseRepository, never()).delete(course);
    }

    @Test
    void createCourse_ShouldCalculatePriceBasedOnAuthorLevel() {
        CreateCourseRequest request = Instancio.create(CreateCourseRequest.class);
        String username = "john.doe";
        User author = Instancio.create(User.class);
        author.setLevel(5);
        User moderator = Instancio.create(User.class);
        Course course = Instancio.create(Course.class);
        Course savedCourse = Instancio.create(Course.class);
        CourseDto expectedDto = Instancio.create(CourseDto.class);

        when(getUserService.getByUsernameOrElseThrow(username)).thenReturn(author);
        when(moderatorManager.assignModerator()).thenReturn(moderator);
        when(courseMapper.toEntity(eq(request), eq(author), eq(moderator), any(Integer.class)))
                .thenReturn(course);
        when(courseRepository.save(course)).thenReturn(savedCourse);
        when(courseMapper.toDto(savedCourse)).thenReturn(expectedDto);

        courseService.createCourse(request, username);

        ArgumentCaptor<Integer> priceCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(courseMapper).toEntity(eq(request), eq(author), eq(moderator), priceCaptor.capture());

        Integer calculatedPrice = priceCaptor.getValue();
        assertThat(calculatedPrice).isPositive();
    }

    @Test
    void searchCourses_ShouldHandleNullFilters() {
        Course course = Instancio.create(Course.class);
        List<Course> courses = List.of(course);
        Page<Course> coursePage = new PageImpl<>(courses, pageable, 1);
        CourseDto dto = Instancio.create(CourseDto.class);

        var request = new CourseSearchRequest(null,null, null,null,null);
        when(courseRepository.searchCourses(null, null, null, null, null, pageable))
                .thenReturn(coursePage);
        when(courseMapper.toDto(course)).thenReturn(dto);

        Page<CourseDto> result = courseService.searchCourses(request, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(dto);
    }

    @Test
    void getCourseDtoByIdOrElseThrow_ShouldReturnCourseDto_WhenCourseExists() {
        Long courseId = 1L;
        Course course = Instancio.create(Course.class);
        CourseDto expectedDto = Instancio.create(CourseDto.class);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseMapper.toDto(course)).thenReturn(expectedDto);

        CourseDto result = courseService.getCourseDtoByIdOrElseThrow(courseId);

        assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    void getCourseDtoByIdOrElseThrow_ShouldThrowEntityNotFoundException_WhenCourseDoesNotExist() {
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseDtoByIdOrElseThrow(courseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course not found with id: " + courseId);
    }

    @Test
    void getCourseByIdOrElseThrow_ShouldReturnCourse_WhenCourseExists() {
        Long courseId = 1L;
        Course expectedCourse = Instancio.create(Course.class);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(expectedCourse));

        Course result = courseService.getCourseByIdOrElseThrow(courseId);

        assertThat(result).isEqualTo(expectedCourse);
    }

    @Test
    void getCourseByIdOrElseThrow_ShouldThrowEntityNotFoundException_WhenCourseDoesNotExist() {
        Long courseId = 1L;
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseByIdOrElseThrow(courseId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Course not found with id: " + courseId);
    }

    @Test
    void findAllTags_ShouldReturnStreamOfTagArrays() {
        String[] tags1 = {"java", "spring"};
        String[] tags2 = {"python", "django"};
        Stream<String[]> expectedStream = Stream.of(tags1, tags2);
        when(courseRepository.getTags()).thenReturn(expectedStream);

        Stream<String[]> result = courseService.findAllTags();

        assertThat(result).isEqualTo(expectedStream);
        verify(courseRepository).getTags();
    }

    @Test
    void findAllByModerator_ShouldReturnListOfCourses() {
        User moderator = Instancio.create(User.class);
        Course course1 = Instancio.create(Course.class);
        Course course2 = Instancio.create(Course.class);
        List<Course> expectedCourses = List.of(course1, course2);
        when(courseRepository.findAllByModerator(moderator)).thenReturn(expectedCourses);

        List<Course> result = courseService.findAllByModerator(moderator);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(course1, course2);
    }

    @Test
    void findAllByModerator_ShouldReturnEmptyList_WhenModeratorHasNoCourses() {
        User moderator = Instancio.create(User.class);
        when(courseRepository.findAllByModerator(moderator)).thenReturn(List.of());

        List<Course> result = courseService.findAllByModerator(moderator);

        assertThat(result).isEmpty();
    }
}