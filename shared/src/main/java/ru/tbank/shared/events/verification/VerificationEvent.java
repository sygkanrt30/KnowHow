package ru.tbank.shared.events.verification;

import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;

import java.util.Objects;

public class VerificationEvent extends AbstractEvent {

    private final String code;

    public VerificationEvent(String contact, String code) {
        super(EventType.VERIFICATION, contact);
        validateCode(code);
        this.code = code;
    }

    private void validateCode(String code) {
        if (Objects.isNull(code) || code.length() != 6) {
            throw new IllegalArgumentException("Invalid code");
        }
    }

    public String getCode() {
        return code;
    }
}
