package ru.tbank.knowhow.core_service.service.moderation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.core_service.model.courses.Course;
import ru.tbank.knowhow.core_service.model.courses.CourseStatus;
import ru.tbank.knowhow.core_service.model.users.User;
import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;
import ru.tbank.knowhow.core_service.mappers.CourseMapper;
import ru.tbank.knowhow.core_service.service.courses.GetCourseService;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ModerationServiceImpl implements ModerationService {

    private final GetUserService getUserService;
    private final GetCourseService getCourseService;
    private final CourseMapper courseMapper;

    @Override
    public List<CourseDto> findAllCoursesOnModerationByModeratorId(Long moderationId) {
        User moderator = getUserService.getByIdOrElseThrow(moderationId);

        List<Course> courses =  getCourseService.findAllByModerator(moderator);

        return courses.stream()
                .filter(course -> course.getStatus().equals(CourseStatus.ON_MODERATION))
                .map(courseMapper::toDto)
                .toList();
    }
}
