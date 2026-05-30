package ru.tbank.knowhow.core_service.model.dto.user.response;

import ru.tbank.knowhow.core_service.model.dto.user.balance.request.BalanceDto;
import ru.tbank.knowhow.core_service.model.users.Role;

public record UsernameAndBalanceResponse(
        String username,
        Role role,
        BalanceDto balance
) {
}
