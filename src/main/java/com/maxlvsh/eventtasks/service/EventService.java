package com.maxlvsh.eventtasks.service;


import com.maxlvsh.eventtasks.dto.EventRequest;
import com.maxlvsh.eventtasks.entity.EventEntity;
import com.maxlvsh.eventtasks.entity.EventStatus;
import com.maxlvsh.eventtasks.repository.EventRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
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
        if (eventRepository.findByExternalId(request.externalId()).isPresent())
            throw new EntityExistsException("Event with externalId " + request.externalId() + " already exists");

        metricsService.incrementReceived();

        EventEntity event = new EventEntity();
        event.setExternalId(request.externalId());
        event.setType(request.type());
        event.setPayload(request.payload());
        event.setStatus(EventStatus.NEW);

        EventEntity savedEvent = eventRepository.save(event);
        log.info("Saved event to DB: {} with externalId: {}", savedEvent.getId(), savedEvent.getExternalId());

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
                .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<EventEntity> getEventsByStatus(EventStatus status) {
        if (status == null) {
            return eventRepository.findAll();
        }
        return eventRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public EventEntity getLatestProcessedByType(String type) {
        return eventRepository.findLatestProcessedByType(type)
                .orElseThrow(() -> new EntityNotFoundException("No processed event found with type: " + type));
    }
}
