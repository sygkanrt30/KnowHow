package ru.tbank.knowhow.service.moder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.model.Course;
import ru.tbank.knowhow.model.CourseStatus;
import ru.tbank.knowhow.model.User;
import ru.tbank.knowhow.model.dto.response.CourseDto;
import ru.tbank.knowhow.model.mapper.CourseMapper;
import ru.tbank.knowhow.service.course.GetCourseService;
import ru.tbank.knowhow.service.user.GetUserService;

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
