package ru.tbank.knowhow.model.dto.response;

public record RatingDto(
        Long id,
        Short grade,
        Long userId,
        CourseDto course
) {
}
