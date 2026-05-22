package ru.tbank.knowhow.service.moder;

import ru.tbank.knowhow.model.dto.response.CourseDto;

import java.util.List;

public interface ModerationService {

    List<CourseDto> findAllCoursesOnModerationByModeratorId(Long moderationId);
}
