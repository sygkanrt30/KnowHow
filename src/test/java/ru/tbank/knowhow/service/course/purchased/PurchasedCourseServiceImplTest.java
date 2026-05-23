package ru.tbank.knowhow.service.course.purchased;

import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.PurchasedCourse;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.repository.PurchasedCourseRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class PurchasedCourseServiceImplTest {

    @Mock
    private PurchasedCourseRepository purchasedCourseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private PurchasedCourseServiceImpl purchasedCourseService;

    @Test
    void findAllPurchasedCourses_ShouldReturnListOfCourseDtos_WhenUserHasPurchasedCourses() {
        Long userId = 1L;
        var purchasedCourses = new ArrayList<PurchasedCourse>();
        var courseDtos = new ArrayList<CourseDto>();

        for (int i = 0; i < 3; i++) {
            var course = Instancio.create(Course.class);

            PurchasedCourse purchasedCourse = new PurchasedCourse();
            purchasedCourse.setCourse(course);
            purchasedCourses.add(purchasedCourse);

            var dto = Instancio.create(CourseDto.class);
            courseDtos.add(dto);

            when(courseMapper.toDto(course)).thenReturn(dto);
        }
        when(purchasedCourseRepository.findPurchasedCoursesByUserId(userId))
                .thenReturn(purchasedCourses);

        List<CourseDto> result = purchasedCourseService.findAllPurchasedCourses(userId);

        assertThat(result)
                .hasSize(3)
                .containsExactly(courseDtos.toArray(new CourseDto[0]));
    }

    @Test
    void findAllPurchasedCourses_ShouldReturnEmptyList_WhenUserHasNoPurchasedCourses() {
        Long userId = 1L;

        when(purchasedCourseRepository.findPurchasedCoursesByUserId(userId))
                .thenReturn(List.of());

        List<CourseDto> result = purchasedCourseService.findAllPurchasedCourses(userId);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteAllPurchasedCoursesByUserId_ShouldDeleteAllPurchasedCourses() {
        Long userId = 1L;

        purchasedCourseService.deleteAllPurchasedCoursesByUserId(userId);

        verify(purchasedCourseRepository).deleteAllPurchasedCoursesByUserId(userId);
    }

    @Test
    void existsPurchasedCourseByCourseId_ShouldReturnTrue_WhenCourseExists() {
        Long courseId = 1L;

        when(purchasedCourseRepository.existsPurchasedCourseByCourseId(courseId))
                .thenReturn(true);

        boolean result = purchasedCourseService.existsPurchasedCourseByCourseId(courseId);

        assertThat(result).isTrue();
    }

    @Test
    void existsPurchasedCourseByCourseId_ShouldReturnFalse_WhenCourseDoesNotExist() {
        Long courseId = 1L;

        when(purchasedCourseRepository.existsPurchasedCourseByCourseId(courseId))
                .thenReturn(false);

        boolean result = purchasedCourseService.existsPurchasedCourseByCourseId(courseId);

        assertThat(result).isFalse();
    }

    @Test
    void existsPurchasedCourse_ShouldReturnTrue_WhenPurchasedCourseExists() {
        Long courseId = 1L;
        Long userId = 2L;

        when(purchasedCourseRepository.existsPurchasedCourseByCourseIdAndUserId(courseId, userId))
                .thenReturn(true);

        boolean result = purchasedCourseService.existsPurchasedCourse(courseId, userId);

        assertThat(result).isTrue();
    }

    @Test
    void existsPurchasedCourse_ShouldReturnFalse_WhenPurchasedCourseDoesNotExist() {
        Long courseId = 1L;
        Long userId = 2L;

        when(purchasedCourseRepository.existsPurchasedCourseByCourseIdAndUserId(courseId, userId))
                .thenReturn(false);

        boolean result = purchasedCourseService.existsPurchasedCourse(courseId, userId);

        assertThat(result).isFalse();
    }
}