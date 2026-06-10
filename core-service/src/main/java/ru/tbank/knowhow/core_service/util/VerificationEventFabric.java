package ru.tbank.knowhow.core_service.util;

import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.verification.VerificationEvent;

public final class VerificationEventFabric {

    public Event createVerificationEvent(String contact, String code) {
        return new VerificationEvent(contact, code);
    }
}
