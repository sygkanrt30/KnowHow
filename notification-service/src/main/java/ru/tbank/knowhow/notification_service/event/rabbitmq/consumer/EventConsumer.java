package ru.tbank.knowhow.notification_service.event.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.tbank.knowhow.notification_service.event.rabbitmq.service.notification.moderation.ModerationService;
import ru.tbank.knowhow.notification_service.event.rabbitmq.service.notification.purchase.PurchaseCourseService;
import ru.tbank.knowhow.notification_service.event.rabbitmq.service.verification.VerificationService;
import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.notification.AddCourseForModerationNotificationEvent;
import ru.tbank.shared.events.notification.CoursePurchaseNotificationEvent;
import ru.tbank.shared.events.notification.ResultReviewNotificationEvent;
import ru.tbank.shared.events.verification.VerificationEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final VerificationService verificationService;
    private final ModerationService moderationService;
    private final PurchaseCourseService purchaseCourseService;

    @RabbitListener(queues = "${spring.rabbitmq.notification-service.queues.verification.queue}")
    public void handleVerification(Event event) {
        log.info("Received verification event: {}", event.getEventType());
        if (event instanceof VerificationEvent e) {

        } else {
            log.warn("Received unknown event type: {}", event.getEventType());
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.notification-service.queues.notification.queue}")
    public void handleNotification(Event event) {
        log.info("Received notification event: {}", event.getEventType());

        switch (event) {
            case AddCourseForModerationNotificationEvent e -> {

            }
            case CoursePurchaseNotificationEvent e -> {

            }
            case ResultReviewNotificationEvent e -> {

            }
            default -> log.warn("Unexpected event type in notifications queue: {}",
                    event.getClass().getSimpleName());
        }
    }
}
