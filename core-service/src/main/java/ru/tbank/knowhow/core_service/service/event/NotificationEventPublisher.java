package ru.tbank.knowhow.core_service.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.core_service.model.users.NotificationContact;
import ru.tbank.knowhow.core_service.model.users.UserContact;
import ru.tbank.knowhow.core_service.service.purchased.PurchasedCourseService;
import ru.tbank.knowhow.core_service.util.NotificationEventFabric;
import ru.tbank.shared.events.Event;

import java.util.Objects;

@Service
@Slf4j
public class NotificationEventPublisher {

    private final ApplicationEventPublisher delegate;
    private final NotificationEventFabric eventFabric;
    private final PurchasedCourseService purchasedCourseService;

    public NotificationEventPublisher(ApplicationEventPublisher delegate,
                                      PurchasedCourseService purchasedCourseService) {
        this.delegate = delegate;
        this.purchasedCourseService = purchasedCourseService;
        this.eventFabric = new NotificationEventFabric();
    }

    public void createAndPublishResultReviewEvent(UserContact userContact, String reviewResult, String username) {
        publishEvent(userContact, username,
                contact -> eventFabric.createResultReviewEvent(contact, reviewResult, username));
    }

    public void createAndPublishCoursePurchaseEvent(UserContact userContact, String authorName, Long courseId) {
        publishEvent(userContact, authorName, contact -> {
            int n = safeGetPurchasedCourseCount(courseId);
            return eventFabric.createCoursePurchaseEvent(contact, n, authorName);
        });
    }

    public void createAndPublishAddCourseForModerationEvent(UserContact userContact, String title,
                                                            String moderatorUsername) {
        publishEvent(userContact, moderatorUsername,
                contact -> eventFabric.createAddCourseForModerationEvent(contact, title, moderatorUsername));
    }

    private void publishEvent(UserContact userContact, String entityName,
                              ThrowingFunction<String, Event> eventCreator) {
        try {
            String contact = getVerifiedContact(userContact);
            if (Objects.nonNull(contact)) {
                Event event = eventCreator.apply(contact);
                delegate.publishEvent(event);
                log.info("Event published: {}", event.getClass().getSimpleName());
            } else {
                log.warn("No verified contact for: {}", entityName);
            }
        } catch (Exception e) {
            log.error("Failed to publish event for: {}", entityName, e);
        }
    }

    private String getVerifiedContact(UserContact userContact) {
        if (Objects.isNull(userContact)) {
            return null;
        }

        try {
            boolean isPrimaryContactIsEmail = NotificationContact.EMAIL
                    .equals(userContact.getPrimaryNotificationContact());

            if (isPrimaryContactIsEmail && userContact.isEmailVerified()) {
                return userContact.getEmail();
            }
            if (userContact.isTgUsernameVerified()) {
                return userContact.getTgUsername();
            }
        } catch (Exception e) {
            log.error("Error getting verified contact", e);
        }
        return null;
    }

    private int safeGetPurchasedCourseCount(Long courseId) {
        try {
            return Objects.nonNull(courseId) ? purchasedCourseService.numberOfPurchaseByCourseId(courseId) : 0;
        } catch (Exception e) {
            log.error("Failed to get purchased course count for courseId: {}", courseId, e);
            return 0;
        }
    }

    @FunctionalInterface
    private interface ThrowingFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
