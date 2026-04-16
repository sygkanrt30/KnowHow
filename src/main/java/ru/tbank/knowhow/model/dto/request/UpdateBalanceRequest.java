package ru.tbank.knowhow.model.dto.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@EqualsAndHashCode
@Getter
@Accessors(fluent = true)
public final class UpdateBalanceRequest {
    private final boolean isIncreaseBalance;
    private final long coins;

    public UpdateBalanceRequest(Boolean isIncreaseBalance, Long coins) {

        this.isIncreaseBalance = validateAndReturnPrimitiveIfValid(isIncreaseBalance);
        this.coins = validateAndReturnPrimitiveIfValid(coins);
    }

    private boolean validateAndReturnPrimitiveIfValid(Boolean isIncreaseBalance) {
        if (Objects.isNull(isIncreaseBalance)) {
            throw new IllegalArgumentException("isIncreaseBalance cannot be null");
        }
        return isIncreaseBalance;
    }

    private long validateAndReturnPrimitiveIfValid(Long coins) {
        if (Objects.isNull(coins)) {
            throw new IllegalArgumentException("isIncreaseBalance cannot be null");
        }
        if (coins <= 0) {
            throw new IllegalArgumentException("coins cannot be negative");
        }
        return coins;
    }
}
