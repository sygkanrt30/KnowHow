package ru.tbank.knowhow.core_service.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.EventType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class NotificationEventHandler {

    private final RabbitTemplate rabbitTemplate;
    private final ExecutorService notificationExecutor;
    private final String exchange;
    private final String notificationRoutingKey;
    private final String verificationRoutingKey;

    public NotificationEventHandler(
            RabbitTemplate rabbitTemplate,
            ExecutorService notificationExecutor,
            @Value("${spring.rabbitmq.notification-service.exchanger}") String exchange,
            @Value("${spring.rabbitmq.notification-service.queues.notification.key}") String notificationRoutingKey,
            @Value("${spring.rabbitmq.notification-service.queues.verification.key}") String verificationRoutingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.notificationExecutor = notificationExecutor;
        this.exchange = exchange;
        this.notificationRoutingKey = notificationRoutingKey;
        this.verificationRoutingKey = verificationRoutingKey;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(Event event) {
        EventType eventType = event.getEventType();
        UUID eventId = event.getEventId();
        CompletableFuture<Void> future = switch (eventType) {
            case NOTIFICATION -> sendAsync(event, exchange, notificationRoutingKey);
            case VERIFICATION -> sendAsync(event, exchange, verificationRoutingKey);
        };
        future.thenAccept(result -> log.debug("Event processed: {}, type: {}", eventId, eventType))
                .exceptionally(throwable -> {
                    log.error("Final failure for event: {}, type: {}", eventId, eventType, throwable);
                    return null;
                });
    }

    private CompletableFuture<Void> sendAsync(Event event, String exchange, String routingKey) {
        return CompletableFuture.runAsync(() -> {
                    EventType eventType = event.getEventType();
                    UUID eventId = event.getEventId();
                    log.debug("Sending event: {} (type: {}) to {}:{}", eventId, eventType, exchange, routingKey);

                    rabbitTemplate.convertAndSend(exchange, routingKey, event);

                    log.info("Event sent: {} -> {}, eventId: {}, type: {}", exchange, routingKey, eventId, eventType);
                    }, notificationExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to send event after retries: {}, exchange: {}, eventId: {}, type: {}",
                            routingKey, exchange, event.getEventId(), event.getEventType(), throwable);
                    return null;
                });
    }
}