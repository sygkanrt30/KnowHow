package ru.tbank.knowhow.model.dto.course.request;

public record UpdateCourseRequest(
        String title,
        String description,
        String courseText,
        String[] tags
) {
}
