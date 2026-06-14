package ru.tbank.shared.events.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;
import ru.tbank.shared.events.NotificationContactType;

import java.util.Objects;

public final class AddCourseForModerationNotificationEvent extends AbstractEvent {

    private final String title;
    private final String username;

    @JsonCreator
    public AddCourseForModerationNotificationEvent(
            @JsonProperty("contact") String contact,
            @JsonProperty("title") String title,
            @JsonProperty("username") String username,
            @JsonProperty("contactType") NotificationContactType contactType) {
        super(EventType.NOTIFICATION, contact, contactType);
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
