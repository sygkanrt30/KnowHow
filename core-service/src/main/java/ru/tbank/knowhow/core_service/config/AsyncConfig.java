package ru.tbank.knowhow.core_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final String THREAD_NAME = "notification-worker";

    @Bean(name = "notificationExecutor")
    public ExecutorService notificationExecutor() {
        return Executors.newFixedThreadPool(10, r -> {
            var t = new Thread(r, THREAD_NAME);
            t.setDaemon(true);
            return t;
        });
    }
}
