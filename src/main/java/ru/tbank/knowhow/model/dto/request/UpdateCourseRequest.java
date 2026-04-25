package ru.tbank.knowhow.model.dto.request;

public record UpdateCourseRequest(
        String title,
        String description,
        String courseText,
        String[] tags
) {
}
