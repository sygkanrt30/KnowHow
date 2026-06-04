package ru.tbank.shared.events.notification;

import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;

import java.util.Objects;

public final class AddCourseForModerationNotificationEvent extends AbstractEvent {

    private final String title;
    private final String username;

    public AddCourseForModerationNotificationEvent(String contact, String title, String username) {
        super(EventType.NOTIFICATION, contact);
        validateParams(title, username);
        this.title = title;
        this.username = username;
    }

    private void validateParams(String title, String username) {
        if (Objects.isNull(title) || title.isEmpty()) {
            throw new IllegalArgumentException("Title must not be null");
        }
        if (Objects.isNull(username) || username.isEmpty()) {
            throw new IllegalArgumentException("Username must not be null");
        }
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }
}
