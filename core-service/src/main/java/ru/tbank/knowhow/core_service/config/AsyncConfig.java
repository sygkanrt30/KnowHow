package ru.tbank.knowhow.core_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Bean(name = "eventPublisherExecutor")
    public Executor eventPublisherExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("publish-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }
}
