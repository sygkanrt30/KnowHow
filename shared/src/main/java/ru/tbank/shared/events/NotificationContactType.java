package ru.tbank.shared.events;

public enum NotificationContactType {
    EMAIL,
    TELEGRAM;


    public static NotificationContactType fromString(String value) {
        for (var type : NotificationContactType.values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification contact: '" + value);
    }

}
