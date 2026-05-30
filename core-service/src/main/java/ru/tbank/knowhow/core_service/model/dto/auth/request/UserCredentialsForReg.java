package ru.tbank.knowhow.core_service.model.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserCredentialsForReg(
        @Pattern(regexp = "^[A-Za-zА-Яа-яЁё][A-Za-zА-Яа-яЁё0-9_]{0,24}$",
                message = "You must provide the correct username")
        @NotBlank
        String username,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)" +
                "(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*[a-zA-Z].*$",
                message = "You must provide the correct password")
        @NotBlank
        String password,

        @NotBlank(message = "Email cannot be empty")
        @Email(message = "You must provide the correct email address")
        String email,

        String moderatorCode
) {
}
