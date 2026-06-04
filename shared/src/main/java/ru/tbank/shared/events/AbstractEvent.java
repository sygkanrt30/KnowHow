package ru.tbank.shared.events;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractEvent implements Event {

    private final UUID eventId;
    private final LocalDateTime createdAt;
    private final EventType eventType;
    private final String contact;

    protected AbstractEvent(EventType eventType, String contact) {
        validateContact(contact);
        this.eventType = eventType;
        this.contact = contact;
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
}
