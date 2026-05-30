package ru.tbank.knowhow.core_service.model.dto.moderation.request;

import jakarta.validation.constraints.NotBlank;

public record ModerationRejectRequest(
        @NotBlank(message = "Причина отклонения не может быть пустой") String reason
) {
}
