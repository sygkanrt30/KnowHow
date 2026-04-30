package ru.tbank.knowhow.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModerationRejectRequest {
    @NotBlank(message = "Причина отклонения не может быть пустой")
    private String reason;
}
