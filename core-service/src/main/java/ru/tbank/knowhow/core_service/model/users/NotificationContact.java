package ru.tbank.knowhow.core_service.model.users;

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
