package ru.tbank.knowhow.core_service.config.rabbitMq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryTemplate;
import ru.tbank.knowhow.core_service.service.messaging.RabbitMQCallbacksHandler;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RabbitMqConfig {

    private final RabbitMQCallbacksHandler callbacksHandler;
    private final RabbitMqPropertyConfig rabbitMqProperty;

    @Bean
    public Queue verificationQueue() {
        return QueueBuilder
                .durable(rabbitMqProperty.getVerificationQueue())
                .ttl(rabbitMqProperty.getVerificationQueueTtl())
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(rabbitMqProperty.getNotificationQueue())
                .build();
    }

    @Bean
    public DirectExchange mainExchange() {
        return new DirectExchange(rabbitMqProperty.getExchange());
    }

    @Bean
    public Binding verificationBinding() {
        return BindingBuilder.bind(verificationQueue())
                .to(mainExchange())
                .with(rabbitMqProperty.getVerificationQueueRoutingKey());
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(mainExchange())
                .with(rabbitMqProperty.getNotificationQueueRoutingKey());
    }

    @Bean
    public MessageConverter jsonMessageConverter(@Qualifier("rabbitJsonMapper") JsonMapper jsonMapper) {
        var converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter,
                                         @Qualifier("retryTemplateForRabbit") RetryTemplate retryTemplate) {

        var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRetryTemplate(retryTemplate);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(callbacksHandler::handleConfirmCallback);
        rabbitTemplate.setReturnsCallback(callbacksHandler::handleReturnsCallback);
        return rabbitTemplate;
    }
}