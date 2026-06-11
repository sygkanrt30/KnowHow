package ru.tbank.knowhow.core_service.util;

import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.verification.VerificationEvent;
import ru.tbank.shared.events.NotificationContactType;

public final class VerificationEventFabric {

    public Event createVerificationEvent(String contact, String code, NotificationContactType type) {
        return new VerificationEvent(contact, code, type);
    }
}
