package ru.tbank.knowhow.model.dto.user.balance.request;

public record BalanceDto(
        Long id,
        Long userId,
        Long coins) {
}
