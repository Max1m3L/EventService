package com.maxlvsh.eventtasks.controller;

import com.maxlvsh.eventtasks.dto.EventRequest;
import com.maxlvsh.eventtasks.entity.EventEntity;
import com.maxlvsh.eventtasks.service.EventService;
import com.maxlvsh.eventtasks.service.MetricsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final MetricsService metricsService;

    @Autowired
    public EventController(EventService eventService, MetricsService metricsService) {
        this.eventService = eventService;
        this.metricsService = metricsService;
    }

    @PostMapping
    public ResponseEntity<EventEntity> createEvents(@Valid @RequestBody EventRequest request) {
        EventEntity created = eventService.createEvent(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventEntity> getEvent(@PathVariable Long id) {
        EventEntity event = eventService.getEvent(id);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<EventEntity>> getEventsByStatus(
            @RequestParam(required = false) EventEntity.EventStatus status) {
        return ResponseEntity.ok(eventService.getEventsByStatus(status));
    }

    @GetMapping("/latest/{type}")
    public ResponseEntity<EventEntity> getLatestProcessedByType(@PathVariable String type) {
        EventEntity event = eventService.getLatestProcessedByType(type);
        return ResponseEntity.ok(event);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Long>> getMetrics() {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("received", metricsService.getEventsReceived().get());
        metrics.put("processed", metricsService.getEventsProcessed().get());
        metrics.put("failed", metricsService.getEventsFailed().get());
        metrics.put("queueSize", metricsService.getQueueSize().get());
        return ResponseEntity.ok(metrics);
    }
}
