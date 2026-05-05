package ru.tbank.knowhow.model.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record CourseSearchRequest(
        String title,
        String[] tags,
        String authorName,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {}