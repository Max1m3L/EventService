package com.maxlvsh.eventtasks.service;

import com.maxlvsh.eventtasks.entity.EventEntity;
import com.maxlvsh.eventtasks.repository.EventRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final BlockingQueue<EventEntity> queue;
    private final EventRepository eventRepository;
    private final FileEventLogger fileEventLogger;
    private final MetricsService metricsService;
    private final Executor consumerTaskExecutor;
    private final AtomicBoolean running = new AtomicBoolean(true);

    @Autowired
    public EventConsumer(BlockingQueue<EventEntity> queue,
                         EventRepository eventRepository,
                         FileEventLogger fileEventLogger,
                         MetricsService metricsService,
                         @Qualifier("consumerTaskExecutor") Executor consumerTaskExecutor) {
        this.queue = queue;
        this.eventRepository = eventRepository;
        this.fileEventLogger = fileEventLogger;
        this.metricsService = metricsService;
        this.consumerTaskExecutor = consumerTaskExecutor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumers() {
        log.info("Starting event consumers...");
        for (int i = 0; i < 2; i++) {
            consumerTaskExecutor.execute(this::consumeEvents);
        }
        log.info("Started 2 consumers");
    }

    private void consumeEvents() {
        while (running.get()) {
            try {
                EventEntity event = queue.poll();
                metricsService.updateQueueSize(queue.size());

                if (event != null) {
                    processEvent(event);
                }

            } catch (Exception e) {
                log.error("Error in consumer thread: {}", e.getMessage());
            }
        }
        log.info("Consumer stopped");
    }

    private void processEvent(EventEntity event) {
        log.info("Processing event: {} with externalId: {}", event.getId(), event.getExternalId());

        try {
            Thread.sleep(100);

            event.setStatus(EventEntity.EventStatus.PROCESSED);
            event.setProcessedAt(LocalDateTime.now());

            eventRepository.save(event);
            metricsService.incrementProcessed();

            fileEventLogger.logEventProcessing(
                    event.getId(),
                    event.getExternalId(),
                    "PROCESSED",
                    "Event processed successfully"
            );

            log.info("Successfully processed event: {}", event.getExternalId());

        } catch (Exception e) {
            log.error("Failed to process event {}: {}", event.getExternalId(), e.getMessage());

            event.setStatus(EventEntity.EventStatus.FAILED);
            event.setProcessedAt(LocalDateTime.now());
            eventRepository.save(event);
            metricsService.incrementFailed();

            fileEventLogger.logEventProcessing(
                    event.getId(),
                    event.getExternalId(),
                    "FAILED",
                    "Error: " + e.getMessage()
            );
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down consumers...");
        running.set(false);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("All consumers stopped");
    }
}
