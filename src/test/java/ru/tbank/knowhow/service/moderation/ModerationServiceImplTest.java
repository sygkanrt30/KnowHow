package ru.tbank.knowhow.service.moderation;

import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.model.courses.Course;
import ru.tbank.knowhow.model.courses.CourseStatus;
import ru.tbank.knowhow.model.users.User;
import ru.tbank.knowhow.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.mappers.CourseMapper;
import ru.tbank.knowhow.service.courses.GetCourseService;
import ru.tbank.knowhow.service.users.GetUserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class ModerationServiceImplTest {

    @Mock
    private GetUserService getUserService;

    @Mock
    private GetCourseService getCourseService;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private ModerationServiceImpl moderationService;

    @Test
    void findAllCoursesOnModerationByModeratorId_ShouldReturnOnlyCoursesWithOnModerationStatus() {
        Long moderatorId = 1L;
        User moderator = Instancio.create(User.class);

        Course course1 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.ON_MODERATION)
                .create();
        Course course2 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.PASSED_MODERATION)
                .create();
        Course course3 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.ON_MODERATION)
                .create();
        Course course4 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.NOT_ACCEPTED)
                .create();

        List<Course> allCourses = List.of(course1, course2, course3, course4);
        CourseDto dto1 = Instancio.create(CourseDto.class);
        CourseDto dto3 = Instancio.create(CourseDto.class);
        when(getUserService.getByIdOrElseThrow(moderatorId)).thenReturn(moderator);
        when(getCourseService.findAllByModerator(moderator)).thenReturn(allCourses);
        when(courseMapper.toDto(course1)).thenReturn(dto1);
        when(courseMapper.toDto(course3)).thenReturn(dto3);

        List<CourseDto> result = moderationService.findAllCoursesOnModerationByModeratorId(moderatorId);

        assertThat(result)
                .hasSize(2)
                .containsExactly(dto1, dto3);
    }

    @Test
    void findAllCoursesOnModerationByModeratorId_ShouldReturnEmptyList_WhenNoCoursesOnModeration() {
        Long moderatorId = 1L;
        User moderator = Instancio.create(User.class);

        Course course1 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.PASSED_MODERATION)
                .create();
        Course course2 = Instancio.of(Course.class)
                .set(field(Course::getStatus), CourseStatus.NOT_ACCEPTED)
                .create();

        List<Course> allCourses = List.of(course1, course2);
        when(getUserService.getByIdOrElseThrow(moderatorId)).thenReturn(moderator);
        when(getCourseService.findAllByModerator(moderator)).thenReturn(allCourses);

        List<CourseDto> result = moderationService.findAllCoursesOnModerationByModeratorId(moderatorId);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllCoursesOnModerationByModeratorId_ShouldReturnEmptyList_WhenNoCoursesAtAll() {
        Long moderatorId = 1L;
        User moderator = Instancio.create(User.class);
        when(getUserService.getByIdOrElseThrow(moderatorId)).thenReturn(moderator);
        when(getCourseService.findAllByModerator(moderator)).thenReturn(List.of());

        List<CourseDto> result = moderationService.findAllCoursesOnModerationByModeratorId(moderatorId);

        assertThat(result).isEmpty();
    }
}