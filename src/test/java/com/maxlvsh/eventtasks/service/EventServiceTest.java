package com.maxlvsh.eventtasks.service;

import com.maxlvsh.eventtasks.dto.EventRequest;
import com.maxlvsh.eventtasks.entity.EventEntity;
import com.maxlvsh.eventtasks.entity.EventStatus;
import com.maxlvsh.eventtasks.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private MetricsService metricsService;

    private BlockingQueue<EventEntity> eventQueue;

    @InjectMocks
    private EventService eventService;

    private EventRequest validRequest;
    private EventEntity savedEvent;

    @BeforeEach
    void setUp() {
        eventQueue = new LinkedBlockingQueue<>(10);
        eventService = new EventService(eventRepository, eventQueue, metricsService);

        // Готовим тестовые данные
        validRequest = new EventRequest(
                12L,
                "order-447",
                "ORDER_CREATED",
                "{\"amount\": 1000, \"currency\": \"RUB\"}"
        );

        savedEvent = new EventEntity();
        savedEvent.setId(1L);
        savedEvent.setExternalId("test-123");
        savedEvent.setType("TEST");
        savedEvent.setPayload("{\"key\":\"value\"}");
        savedEvent.setStatus(EventStatus.NEW);
    }

    @Test
    void createEvent_WithValidData_ShouldSaveAndQueueEvent() {
        when(eventRepository.save(any(EventEntity.class))).thenReturn(savedEvent);

        EventEntity result = eventService.createEvent(validRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getExternalId()).isEqualTo("test-123");
        assertThat(result.getStatus()).isEqualTo(EventStatus.NEW);

        verify(eventRepository, times(1)).save(any(EventEntity.class));
        verify(metricsService, times(1)).incrementReceived();
    }

    @Test
    void getEvent_WhenExists_ShouldReturnEvent() {
        // Arrange
        when(eventRepository.findById(1L)).thenReturn(Optional.of(savedEvent));

        // Act
        EventEntity result = eventService.getEvent(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getExternalId()).isEqualTo("test-123");
    }

    @Test
    void getEvent_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> eventService.getEvent(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getEventsByStatus_WithNullStatus_ShouldReturnAll() {
        // Act
        eventService.getEventsByStatus(null);

        // Assert
        verify(eventRepository, times(1)).findAll();
        verify(eventRepository, never()).findByStatus(any());
    }

    @Test
    void getEventsByStatus_WithStatus_ShouldReturnFiltered() {
        // Act
        eventService.getEventsByStatus(EventStatus.PROCESSED);

        // Assert
        verify(eventRepository, times(1)).findByStatus(EventStatus.PROCESSED);
        verify(eventRepository, never()).findAll();
    }
}
