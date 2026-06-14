package ru.tbank.knowhow.notification_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@Slf4j
public class RabbitMqConfig {

    @Bean
    public MessageConverter jsonMessageConverter(@Qualifier("rabbitJsonMapper") JsonMapper jsonMapper) {
        var converter = new JacksonJsonMessageConverter(jsonMapper);
        converter.setCreateMessageIds(true);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter,
            @Value("${spring.rabbitmq.concurrency.min}") int minConcurrentConsumer,
            @Value("${spring.rabbitmq.concurrency.max}") int maxConcurrentConsumer) {

        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setTaskExecutor(getSimpleAsyncTaskExecutor());

        factory.setConcurrentConsumers(minConcurrentConsumer);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumer);

        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);

        log.info("RabbitMQ consumer container factory configured with virtual threads");
        return factory;
    }

    private SimpleAsyncTaskExecutor getSimpleAsyncTaskExecutor() {
        var executor = new SimpleAsyncTaskExecutor("rabbit-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
