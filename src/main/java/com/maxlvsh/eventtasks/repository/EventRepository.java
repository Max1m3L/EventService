package com.maxlvsh.eventtasks.repository;

import com.maxlvsh.eventtasks.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long> {
    List<EventEntity> findByStatus(EventEntity.EventStatus status);

    @Query("SELECT e FROM EventEntity e WHERE e.type = :type AND e.status = 'PROCESSED' ORDER BY e.processedAt DESC LIMIT 1")
    Optional<EventEntity> findLatestProcessedByType(@Param("type") String type);

    Integer deleteByCreatedAtBefore(LocalDateTime date);

    Optional<EventEntity> findByExternalId(String externalId);
}
