package ru.tbank.knowhow.model.dto.moderation.request;

import jakarta.validation.constraints.NotBlank;

public record ModerationRejectRequest(
        @NotBlank(message = "Причина отклонения не может быть пустой") String reason
) {
}
