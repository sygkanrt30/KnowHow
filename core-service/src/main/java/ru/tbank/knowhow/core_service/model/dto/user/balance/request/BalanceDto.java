package ru.tbank.knowhow.core_service.model.dto.user.balance.request;

public record BalanceDto(
        Long id,
        Long userId,
        Long coins) {
}
