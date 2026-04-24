package ru.tbank.knowhow.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
        @NotBlank(message = "Название не может быть пустым")
        String title,

        @NotBlank(message = "Описание не может быть пустым")
        String description,

        @NotBlank(message = "Текст курса не может быть пустым")
        String courseText,

        String[] tags
) {}