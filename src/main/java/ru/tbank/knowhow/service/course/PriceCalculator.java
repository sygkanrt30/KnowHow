package ru.tbank.knowhow.service.course;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
final class PriceCalculator {

    private final int priceMultiplier;

    Integer calculate(Integer userLevel) {
        return priceMultiplier * (Objects.nonNull(userLevel) ? userLevel : 1);
    }
}
