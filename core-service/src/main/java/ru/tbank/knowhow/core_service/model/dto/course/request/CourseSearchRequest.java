package ru.tbank.knowhow.core_service.model.dto.course.request;

import java.math.BigDecimal;

public record CourseSearchRequest(
        String title,
        String[] tags,
        String authorName,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {}