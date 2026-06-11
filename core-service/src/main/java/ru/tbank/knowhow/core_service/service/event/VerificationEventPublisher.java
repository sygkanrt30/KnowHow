package ru.tbank.knowhow.core_service.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.core_service.util.VerificationEventFabric;
import ru.tbank.shared.events.Event;
import ru.tbank.shared.events.NotificationContactType;

import java.util.Objects;

@Service
@Slf4j
public class VerificationEventPublisher {

    private final ApplicationEventPublisher delegate;
    private final VerificationEventFabric eventFabric;

    public VerificationEventPublisher(ApplicationEventPublisher delegate) {
        this.delegate = delegate;
        this.eventFabric = new VerificationEventFabric();
    }

    public void createAndPublishVerificationEvent(String contact, String code, NotificationContactType type) {
        try {
            if (Objects.nonNull(contact)) {
                Event event = eventFabric.createVerificationEvent(contact, code, type);
                delegate.publishEvent(event);
                log.info("Event published: {}", event.getClass().getSimpleName());
            }
        } catch (Exception e) {
            log.error("Failed to publish event for: {}", contact, e);
        }
    }
}
