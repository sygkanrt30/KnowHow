package ru.tbank.shared.events;

import java.time.LocalDateTime;
import java.util.UUID;

public interface Event {

    UUID getEventId();

    LocalDateTime getCreatedAt();

    EventType getEventType();
}
