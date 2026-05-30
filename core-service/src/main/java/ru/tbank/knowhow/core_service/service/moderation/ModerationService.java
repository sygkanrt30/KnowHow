package ru.tbank.knowhow.core_service.service.moderation;

import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

import java.util.List;

public interface ModerationService {

    List<CourseDto> findAllCoursesOnModerationByModeratorId(Long moderationId);
}
