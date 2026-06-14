package ru.tbank.knowhow.notification_service.event.rabbitmq.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.tbank.knowhow.notification_service.event.rabbitmq.ProcessingStatisticsCollector;
import ru.tbank.knowhow.notification_service.service.notification.moderation.ModerationService;
import ru.tbank.knowhow.notification_service.service.notification.purchase.PurchaseCourseService;
import ru.tbank.knowhow.notification_service.service.verification.VerificationService;
import ru.tbank.shared.events.AbstractEvent;
import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.NotificationContactType;
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
    private final ProcessingStatisticsCollector statisticsCollector;

    @RabbitListener(queues = "${spring.rabbitmq.queues.verification.queue}")
    public void handleVerification(Event event) {
        log.info("Received verification event: {}", event.getEventType());
        if (event instanceof VerificationEvent e) {
            sendVerification(e);
        } else {
            log.warn("Received unknown event type: {}", event.getEventType());
            statisticsCollector.recordFailure();
        }
    }

    private void sendVerification(VerificationEvent e) {
        try {
            switch (e.getContactType()) {
                case EMAIL -> verificationService.sendVerificationCodeOnEmail(e.getContact());
                case TELEGRAM -> verificationService.sendVerificationCodeOnTg(e.getContact());
            }
        } catch (Exception ex) {
            statisticsCollector.recordFailure();
            log.error(ex.getMessage(), ex);
        }
        statisticsCollector.recordSuccess();
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.notification.queue}")
    public void handleNotification(Event event) {
        log.info("Received notification event: {}", event.getEventType());
        mapEventAndNotify(event);
    }

    private void mapEventAndNotify(Event event) {
        try {
            AbstractEvent abstractEvent = (AbstractEvent) event;
            NotificationContactType contactType = abstractEvent.getContactType();
            String contact = abstractEvent.getContact();
            switch (event) {
                case AddCourseForModerationNotificationEvent e -> {
                    moderationService.notifyModeratorCourseAddOnModeration(contact,
                            e.getTitle(),
                            e.getUsername(),
                            contactType);
                    statisticsCollector.recordSuccess();
                }
                case CoursePurchaseNotificationEvent e -> {
                    purchaseCourseService.notifyAuthorCoursePurchase(contact,
                            e.getNumberOfPurchasedCourse(),
                            e.getAuthorName(),
                            contactType);
                    statisticsCollector.recordSuccess();
                }
                case ResultReviewNotificationEvent e -> {
                    moderationService.notifyUserResultOfModeration(contact,
                            e.getReviewResult(),
                            e.getUsername(),
                            contactType);
                    statisticsCollector.recordSuccess();
                }
                default -> {
                    log.warn("Unexpected event type in notifications queue: {}",
                            event.getClass().getSimpleName());
                    statisticsCollector.recordFailure();
                }
            }
        } catch (Exception ex) {
            statisticsCollector.recordFailure();
            log.error(ex.getMessage(), ex);
        }
    }
}
