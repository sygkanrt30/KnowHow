package ru.tbank.knowhow.model.dto.rating.response;

import ru.tbank.knowhow.model.dto.course.response.CourseDto;

public record RatingDto(
        Long id,
        Integer grade,
        Long userId,
        CourseDto course
) {
}
