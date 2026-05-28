package ru.tbank.knowhow.model;

public enum NotificationContact {
    EMAIL,
    TELEGRAM;


    public static NotificationContact fromString(String value) {
        for (var type : NotificationContact.values()) {
            if (type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown notification contact: '" + value);
    }

}
