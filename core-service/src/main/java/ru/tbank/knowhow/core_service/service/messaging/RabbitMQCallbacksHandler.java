package ru.tbank.knowhow.core_service.service.messaging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tbank.shared.events.Event;
import tools.jackson.databind.json.JsonMapper;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static ru.tbank.knowhow.core_service.service.messaging.RabbitMqCallbackMDCContextKey.*;

@Slf4j
@Service
public class RabbitMQCallbacksHandler {

    private final JsonMapper jsonMapper;
    private final AtomicLong successConfirms;
    private final AtomicLong failedConfirms;

    @Autowired
    public RabbitMQCallbacksHandler(@Qualifier("rabbitJsonMapper") JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.successConfirms = new AtomicLong();
        this.failedConfirms = new AtomicLong();
    }

    public void handleConfirmCallback(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            successConfirms.incrementAndGet();
        } else {
            failedConfirms.incrementAndGet();
            String messageId = Objects.nonNull(correlationData) ? correlationData.getId() : "unknown";
            log.error("RabbitMQ publish NACK. messageId={}, cause={}", messageId, cause);
        }
    }

    public void handleReturnsCallback(ReturnedMessage returned) {
        try {
            Message message = returned.getMessage();
            var notificationEvent = jsonMapper.readValue(message.getBody(), Event.class);

            setupMDC(returned, notificationEvent, message);
            log.error(buildHumanReadableLog(returned, notificationEvent));

        } catch (Exception e) {
            log.error("Failed to parse returned message body", e);
        } finally {
            MDC.clear();
        }
    }

    private void setupMDC(ReturnedMessage returned, Event event, Message message) {
        MDC.put(REPLY_CODE.value(), String.valueOf(returned.getReplyCode()));
        MDC.put(EVENT_ID.value(), event.getEventId().toString());
        MDC.put(EVENT_TYPE.value(), event.getEventType().toString());

        if (Objects.nonNull(message.getMessageProperties().getMessageId())) {
            MDC.put(MESSAGE_ID.value(), message.getMessageProperties().getMessageId());
        }
    }

    private String buildHumanReadableLog(ReturnedMessage returned, Event event) {
        return String.format("""
                        ✗ Message returned from broker
                          └─ Reply: %d - %s
                          └─ Exchange: %s -> %s
                          └─ Event: %s (type=%s, created=%s)
                        """,
                returned.getReplyCode(),
                returned.getReplyText(),
                returned.getExchange(),
                returned.getRoutingKey(),
                event.getEventId(),
                event.getEventType(),
                event.getCreatedAt()
        );
    }

    @Scheduled(
            fixedDelayString = "${spring.rabbitmq.confirms.statistic.log.delay.min.fixed}",
            initialDelayString = "${spring.rabbitmq.confirms.statistic.log.delay.min.initial}",
            timeUnit = TimeUnit.MINUTES
    )
    public void logConfirmStats() {
        if (failedConfirms.get() > 0 || successConfirms.get() > 0) {
            log.info("RabbitMQ confirms - success: {}, failed: {}",
                    successConfirms.get(), failedConfirms.get());
        }
    }

    public long getSuccessfulConfirms() {
        return successConfirms.get();
    }

    public long getFailedConfirms() {
        return failedConfirms.get();
    }
}
