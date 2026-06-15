package ru.tbank.shared.events.notification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.EventType;
import ru.tbank.shared.events.ReviewResult;

import java.util.Objects;

public final class ResultReviewNotificationEvent extends AbstractEvent {

    private final ReviewResult reviewResult;
    private final String username;

    @JsonCreator
    public ResultReviewNotificationEvent(
            @JsonProperty("contact") String contact,
            @JsonProperty("reviewResult") ReviewResult reviewResult,
            @JsonProperty("username") String username) {

        super(EventType.NOTIFICATION, contact);
        validateParams(reviewResult, username);
        this.reviewResult = reviewResult;
        this.username = username;
    }

    private void validateParams(ReviewResult reviewResult, String username) {
        if (Objects.isNull(reviewResult)) {
            throw new IllegalArgumentException("reviewResult must not be null");
        }
        if (Objects.isNull(username)) {
            throw new IllegalArgumentException("username must not be null");
        }
    }

    public ReviewResult getReviewResult() {
        return reviewResult;
    }

    public String getUsername() {
        return username;
    }
}
