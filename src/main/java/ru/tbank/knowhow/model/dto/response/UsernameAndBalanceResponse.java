package ru.tbank.knowhow.model.dto.response;

public record UsernameAndBalanceResponse(
        String username,
        BalanceDto balance
) {
}
