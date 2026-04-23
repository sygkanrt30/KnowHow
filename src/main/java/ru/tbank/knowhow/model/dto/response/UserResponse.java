package ru.tbank.knowhow.model.dto.response;

import ru.tbank.knowhow.model.Role;

public record UserResponse(
        Long id,
        String username,
        String email,
        Role role,
        Long coins
) {
}
