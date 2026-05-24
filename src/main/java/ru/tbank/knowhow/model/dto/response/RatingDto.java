package ru.tbank.knowhow.model.dto.response;

public record RatingDto(
        Long id,
        Integer grade,
        Long userId,
        CourseDto course
) {
}
