package ru.tbank.knowhow.core_service.service.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
@Accessors(fluent = true)
enum RabbitMqCallbackMDCContextKey {
    REPLY_CODE("rabbit.replyCode"),
    EVENT_ID("eventId"),
    EVENT_TYPE("eventType"),
    MESSAGE_ID("message.id");

    private final String value;
}
