package ru.tbank.knowhow.core_service.model.dto.course.response;

import java.math.BigDecimal;

public record CourseDto(
        Long id,
        String title,
        String description,
        String courseText,
        Long price,
        String[] tags,
        BigDecimal rating,
        Long authorId,
        boolean notForSale) {
}
