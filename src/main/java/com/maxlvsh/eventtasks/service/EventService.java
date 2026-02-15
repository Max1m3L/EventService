package com.maxlvsh.eventtasks.service;


import com.maxlvsh.eventtasks.dto.EventRequest;
import com.maxlvsh.eventtasks.entity.EventEntity;
import com.maxlvsh.eventtasks.exception.EventNotFoundException;
import com.maxlvsh.eventtasks.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.BlockingQueue;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final BlockingQueue<EventEntity> eventQueue;
    private final MetricsService metricsService;

    @Autowired
    public EventService(EventRepository eventRepository,
                        BlockingQueue<EventEntity> eventQueue,
                        MetricsService metricsService) {
        this.eventRepository = eventRepository;
        this.eventQueue = eventQueue;
        this.metricsService = metricsService;
    }

    @Transactional
    public EventEntity createEvent(EventRequest request) {
        metricsService.incrementReceived();

        // Создаем сущность
        EventEntity event = new EventEntity();
        event.setExternalId(request.getExternalId());
        event.setType(request.getType());
        event.setPayload(request.getPayload());
        event.setStatus(EventEntity.EventStatus.NEW);

        // Сначала сохраняем в БД
        EventEntity savedEvent = eventRepository.save(event);
        log.info("Saved event to DB: {} with externalId: {}", savedEvent.getId(), savedEvent.getExternalId());

        // Пытаемся положить в очередь
        boolean queued = eventQueue.offer(savedEvent);

        if (!queued) {
            log.warn("Queue is full, event {} will be processed later", savedEvent.getId());
        } else {
            log.debug("Event {} added to processing queue", savedEvent.getId());
        }

        return savedEvent;
    }

    @Transactional(readOnly = true)
    public EventEntity getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<EventEntity> getEventsByStatus(EventEntity.EventStatus status) {
        if (status == null) {
            return eventRepository.findAll();
        }
        return eventRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public EventEntity getLatestProcessedByType(String type) {
        return eventRepository.findLatestProcessedByType(type)
                .orElseThrow(() -> new EventNotFoundException("No processed event found with type: " + type));
    }
}
