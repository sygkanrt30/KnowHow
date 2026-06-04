package ru.tbank.knowhow.core_service.config.rabbitMq;

import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.AmqpResourceNotAvailableException;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.backoff.ExponentialBackOff;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class SpringRetryConfig {

    @Bean
    @Qualifier("retryTemplateForRabbit")
    public RetryTemplate rabbitNetworkRetryTemplate(
            @Value("${spring.retry-template.max-retry}") int maxAttempts,
            @Value("${spring.retry-template.back-off.interval.init}") long initInterval,
            @Value("${spring.retry-template.back-off.interval.max}") long maxInterval,
            @Value("${spring.retry-template.back-off.multiplier}") double multiplier) {

        var backOff = new ExponentialBackOff();
        backOff.setInitialInterval(initInterval);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(maxInterval);

        var retryPolicy = RetryPolicy.builder()
                .maxRetries(maxAttempts)
                .backOff(backOff)
                .includes(AmqpConnectException.class, AmqpResourceNotAvailableException.class)
                .excludes(
                        AmqpException.class,
                        IOException.class,
                        TimeoutException.class,
                        MessageConversionException.class,
                        AmqpRejectAndDontRequeueException.class
                )
                .build();
        return new RetryTemplate(retryPolicy);
    }
}