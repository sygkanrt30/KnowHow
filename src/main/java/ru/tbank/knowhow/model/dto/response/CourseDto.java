package ru.tbank.knowhow.model.dto.response;

import java.math.BigDecimal;

public record CourseDto(
        Long id,
        String title,
        String description,
        String courseText,
        Long price,
        String[] tags,
        BigDecimal rating,
        Long authorId) {
}
