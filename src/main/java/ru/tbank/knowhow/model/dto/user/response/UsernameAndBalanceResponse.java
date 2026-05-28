package ru.tbank.knowhow.model.dto.user.response;

import ru.tbank.knowhow.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.model.users.Role;

public record UsernameAndBalanceResponse(
        String username,
        Role role,
        BalanceDto balance
) {
}
