package ru.tbank.knowhow.service.moderation;

import ru.tbank.knowhow.model.dto.course.response.CourseDto;

import java.util.List;

public interface ModerationService {

    List<CourseDto> findAllCoursesOnModerationByModeratorId(Long moderationId);
}
