package ru.tbank.knowhow.model.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
public enum FieldNameToSortBy {
    PRICE("price"),
    RATING("rating");

    private final String propertyName;
}
