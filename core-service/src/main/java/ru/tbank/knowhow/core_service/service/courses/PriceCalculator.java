package ru.tbank.knowhow.core_service.service.courses;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class PriceCalculator {

    private static final int DEFAULT_LEVEL = 1;

    private final int priceMultiplier;

    Integer calculate(Integer userLevel) {
        return priceMultiplier * (Objects.nonNull(userLevel) ? userLevel : DEFAULT_LEVEL);
    }
}
