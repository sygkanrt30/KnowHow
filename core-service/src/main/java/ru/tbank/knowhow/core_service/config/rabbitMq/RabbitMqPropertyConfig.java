package ru.tbank.knowhow.core_service.config.rabbitMq;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RabbitMqPropertyConfig {

    @Value("${spring.rabbitmq.notification-service.exchange}")
    private String exchange;

    @Value("${spring.rabbitmq.notification-service.queues.verification.queue}")
    private String verificationQueue;

    @Value("${spring.rabbitmq.notification-service.queues.notification.queue}")
    private String notificationQueue;

    @Value("${spring.rabbitmq.notification-service.queues.verification.ttl}")
    private int verificationQueueTtl;

    @Value("${spring.rabbitmq.notification-service.queues.verification.key}")
    private String verificationQueueRoutingKey;

    @Value("${spring.rabbitmq.notification-service.queues.notification.key}")
    private String notificationQueueRoutingKey;

}
