package ru.tbank.knowhow.model.dto.response;

import ru.tbank.knowhow.model.Role;

public record UsernameAndBalanceResponse(
        String username,
        Role role,
        BalanceDto balance
) {
}
