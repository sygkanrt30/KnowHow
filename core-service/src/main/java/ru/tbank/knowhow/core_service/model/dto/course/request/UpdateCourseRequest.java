package ru.tbank.knowhow.core_service.model.dto.course.request;

public record UpdateCourseRequest(
        String title,
        String description,
        String courseText,
        String[] tags
) {
}
