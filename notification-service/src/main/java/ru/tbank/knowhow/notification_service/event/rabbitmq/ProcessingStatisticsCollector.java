package ru.tbank.knowhow.notification_service.event.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class ProcessingStatisticsCollector {

    private final AtomicLong successfulAttempts = new AtomicLong(0L);
    private final AtomicLong failedAttempts = new AtomicLong(0L);
    private final AtomicLong totalAttempts = new AtomicLong(0L);
    private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());

    @Scheduled(
            fixedDelayString = "${statistics.logging.interval.minutes}",
            initialDelayString = "${statistics.logging.interval.initial.minutes}",
            timeUnit = TimeUnit.MINUTES)
    public void logStatistics() {
        long success = successfulAttempts.getAndSet(0L);
        long failure = failedAttempts.getAndSet(0L);
        long total = totalAttempts.getAndSet(0L);
        long currentTime = System.currentTimeMillis();
        long timeDiff = (currentTime - lastResetTime.getAndSet(currentTime)) / 1000;

        double successRate = total > 0 ? (success * 100.0 / total) : 0;
        double roundSuccessRate = Math.round(successRate);

        log.info("""
                
                ===== Processing Statistics =====
                Time window: {} seconds
                Total attempts: {}
                Successful: {} ({}%)
                Failed: {} ({}%)
                Throughput: {} msg/sec
                =================================
                """,
                timeDiff,
                total,
                success, roundSuccessRate,
                failure, 100 - roundSuccessRate,
                Math.round(total / (double) timeDiff));
    }

    public void recordSuccess() {
        successfulAttempts.incrementAndGet();
        totalAttempts.incrementAndGet();
    }

    public void recordFailure() {
        failedAttempts.incrementAndGet();
        totalAttempts.incrementAndGet();
    }
}
