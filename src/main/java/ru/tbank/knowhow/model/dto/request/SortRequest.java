package ru.tbank.knowhow.model.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public record SortRequest(
        @NotNull FieldNameToSortBy fieldName,
        Boolean isACS
) {
    public SortRequest {
        if (Objects.isNull(isACS)) isACS = Boolean.TRUE;
    }
}
