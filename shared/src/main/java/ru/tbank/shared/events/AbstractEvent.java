package ru.tbank.shared.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractEvent implements Event {

    private final UUID eventId;
    private final LocalDateTime createdAt;
    private final EventType eventType;
    private final String contact;
    private final NotificationContactType contactType;

    @JsonCreator
    protected AbstractEvent(
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("contact") String contact,
            @JsonProperty("contactType") NotificationContactType contactType
    ) {
        validateContact(contact);
        this.eventType = eventType;
        this.contact = contact;
        this.contactType = contactType;
        this.eventId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    private void validateContact(String contact) {
        if (Objects.isNull(contact)) {
            throw new IllegalArgumentException("Contact must not be null");
        }
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    public String getContact() {
        return contact;
    }

    public NotificationContactType getContactType() {
        return contactType;
    }
}
