package ru.tbank.knowhow.model;

import java.util.Optional;

public enum CommunicationType {
    EMAIL,
    TELEGRAM;


    public Optional<CommunicationType> fromString(String value) {
        for (var type : CommunicationType.values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
