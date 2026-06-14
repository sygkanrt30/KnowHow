package ru.tbank.shared.events.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;
import ru.tbank.shared.events.NotificationContactType;

import java.util.Objects;

public class VerificationEvent extends AbstractEvent {

    private final String code;

    @JsonCreator
    public VerificationEvent(
            @JsonProperty("contact") String contact,
            @JsonProperty("code") String code,
            @JsonProperty("contactType") NotificationContactType type
    ) {
        super(EventType.VERIFICATION, contact, type);
        validateCode(code, type);
        this.code = code;
    }

    private void validateCode(String code, NotificationContactType type) {
        if (Objects.isNull(code) || code.length() != 6) {
            throw new IllegalArgumentException("Invalid code");
        }
        if (Objects.isNull(type)) {
            throw new IllegalArgumentException("Invalid type");
        }
    }

    public String getCode() {
        return code;
    }
}
