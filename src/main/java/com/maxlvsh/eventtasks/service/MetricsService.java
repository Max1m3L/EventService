package com.maxlvsh.eventtasks.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {

    private static final Logger log = LoggerFactory.getLogger(MetricsService.class);

    private final AtomicLong eventsReceived = new AtomicLong(0);
    private final AtomicLong eventsProcessed = new AtomicLong(0);
    private final AtomicLong eventsFailed = new AtomicLong(0);
    private final AtomicLong queueSize = new AtomicLong(0);

    public void incrementReceived() {
        eventsReceived.incrementAndGet();
    }

    public void incrementProcessed() {
        eventsProcessed.incrementAndGet();
    }

    public void incrementFailed() {
        eventsFailed.incrementAndGet();
    }

    public void updateQueueSize(int size) {
        queueSize.set(size);
    }

    public AtomicLong getEventsReceived() {
        return eventsReceived;
    }

    public AtomicLong getEventsProcessed() {
        return eventsProcessed;
    }

    public AtomicLong getEventsFailed() {
        return eventsFailed;
    }

    public AtomicLong getQueueSize() {
        return queueSize;
    }

    public void logMetrics() {
        log.info("=== METRICS ===");
        log.info("Received: {}", eventsReceived.get());
        log.info("Processed: {}", eventsProcessed.get());
        log.info("Failed: {}", eventsFailed.get());
        log.info("Queue size: {}", queueSize.get());
        log.info("===============");
    }
}