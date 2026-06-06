package ru.tbank.knowhow.core_service.service.moderation.verdict;

import jakarta.persistence.EntityExistsException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.courses.CourseStatus;
import ru.tbank.knowhow.core_service.model.moderation.ModerationReview;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.repository.moderation.ModerationReviewRepository;
import ru.tbank.knowhow.core_service.repository.moderation.ModeratorLoadRepository;
import ru.tbank.knowhow.core_service.service.courses.GetCourseService;
import ru.tbank.knowhow.core_service.service.event.NotificationEventPublisher;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class CourseVerdictServiceImplTest {

    @Mock
    private GetUserService getUserService;

    @Mock
    private GetCourseService getCourseService;

    @Mock
    private ModerationReviewRepository moderationReviewRepository;

    @Mock
    private ModeratorLoadRepository moderatorLoadRepository;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @InjectMocks
    private CourseVerdictServiceImpl courseVerdictService;

    @Test
    void approveCourse_ShouldApproveCourseSuccessfully_WhenNotProcessedBefore() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);
        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(false);

        courseVerdictService.approveCourse(courseId, moderatorUsername);

        ArgumentCaptor<ModerationReview> reviewCaptor = ArgumentCaptor.forClass(ModerationReview.class);
        verify(moderationReviewRepository).save(reviewCaptor.capture());

        ModerationReview savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getModerator()).isEqualTo(moderator);
        assertThat(savedReview.getCourse()).isEqualTo(course);
        assertThat(savedReview.getRejectionReason()).isNull();
        verify(moderatorLoadRepository).decrementCoursesInModeration(moderator.getId());
        assertThat(course.getStatus()).isEqualTo(CourseStatus.PASSED_MODERATION);
        assertThat(course.getModerator()).isNull();
    }

    @Test
    void rejectCourse_ShouldRejectCourseSuccessfully_WhenNotProcessedBefore() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Poor quality content";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);
        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(false);

        courseVerdictService.rejectCourse(courseId, moderatorUsername, rejectionReason);

        ArgumentCaptor<ModerationReview> reviewCaptor = ArgumentCaptor.forClass(ModerationReview.class);
        verify(moderationReviewRepository).save(reviewCaptor.capture());

        ModerationReview savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getModerator()).isEqualTo(moderator);
        assertThat(savedReview.getCourse()).isEqualTo(course);
        assertThat(savedReview.getRejectionReason()).isEqualTo(rejectionReason);
        verify(moderatorLoadRepository).decrementCoursesInModeration(moderator.getId());
        assertThat(course.getStatus()).isEqualTo(CourseStatus.NOT_ACCEPTED);
        assertThat(course.getModerator()).isNull();
    }

    @Test
    void approveCourse_ShouldThrowEntityExistsException_WhenCourseAlreadyProcessedByModerator() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);

        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> courseVerdictService.approveCourse(courseId, moderatorUsername))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("Moderator has already processed this course");

        verify(moderationReviewRepository, never()).save(any());
        verify(moderatorLoadRepository, never()).decrementCoursesInModeration(any());
    }

    @Test
    void rejectCourse_ShouldThrowEntityExistsException_WhenCourseAlreadyProcessedByModerator() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Poor quality";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);
        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> courseVerdictService.rejectCourse(courseId, moderatorUsername, rejectionReason))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("Moderator has already processed this course");

        verify(moderationReviewRepository, never()).save(any());
        verify(moderatorLoadRepository, never()).decrementCoursesInModeration(any());
    }

    @Test
    void approveCourse_ShouldDecrementModeratorLoad_WhenSuccessfullyProcessed() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);
        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(false);

        courseVerdictService.approveCourse(courseId, moderatorUsername);

        verify(moderatorLoadRepository).decrementCoursesInModeration(moderator.getId());
    }

    @Test
    void rejectCourse_ShouldDecrementModeratorLoad_WhenSuccessfullyProcessed() {
        Long courseId = 1L;
        String moderatorUsername = "moderator";
        String rejectionReason = "Poor quality";
        User moderator = createModerator(moderatorUsername);
        Course course = createCourse(courseId);
        when(getUserService.getByUsernameOrElseThrow(moderatorUsername)).thenReturn(moderator);
        when(getCourseService.getCourseByIdOrElseThrow(courseId)).thenReturn(course);
        when(moderationReviewRepository.existsByCourseIdAndModeratorId(courseId, moderator.getId()))
                .thenReturn(false);

        courseVerdictService.rejectCourse(courseId, moderatorUsername, rejectionReason);

        verify(moderatorLoadRepository).decrementCoursesInModeration(moderator.getId());
    }

    private User createModerator(String moderatorUsername) {
        return Instancio.of(User.class)
                .set(field(User::getId), 10L)
                .set(field(User::getUsername), moderatorUsername)
                .create();
    }

    private Course createCourse(Long courseId) {
        return Instancio.of(Course.class)
                .set(field(Course::getId), courseId)
                .set(field(Course::getStatus), CourseStatus.ON_MODERATION)
                .create();
    }
}