package ru.tbank.shared.events.verification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;

import java.util.Objects;

public class VerificationEvent extends AbstractEvent {

    private final String code;

    @JsonCreator
    public VerificationEvent(
            @JsonProperty("contact") String contact,
            @JsonProperty("code") String code
    ) {
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
