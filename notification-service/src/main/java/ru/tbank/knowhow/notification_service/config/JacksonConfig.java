package ru.tbank.knowhow.notification_service.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    private static final String EVENT_PACKAGE = "ru.tbank.shared.event.";

    @Bean
    @Qualifier("rabbitJsonMapper")
    public JsonMapper jsonMapper() {
        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(EVENT_PACKAGE)
                .build();

        return JsonMapper.builder()
                .defaultTimeZone(TimeZone.getTimeZone("Europe/Moscow"))
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(ptv, DefaultTyping.NON_FINAL)
                .build();
    }
}
