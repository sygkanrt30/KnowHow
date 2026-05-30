package ru.tbank.knowhow.core_service.model.dto.rating.response;

import ru.tbank.knowhow.core_service.model.dto.course.response.CourseDto;

public record RatingDto(
        Long id,
        Integer grade,
        Long userId,
        CourseDto course
) {
}
