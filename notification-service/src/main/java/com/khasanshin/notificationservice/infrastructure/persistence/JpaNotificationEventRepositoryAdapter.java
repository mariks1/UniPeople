package com.khasanshin.notificationservice.infrastructure.persistence;

import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import com.khasanshin.notificationservice.domain.port.NotificationEventRepositoryPort;
import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaNotificationEventRepositoryAdapter implements NotificationEventRepositoryPort {

    private final SpringDataNotificationEventRepository repo;

    @Override
    public Optional<NotificationEvent> findByEventId(UUID eventId) {
        return repo.findByEventId(eventId).map(this::toDomain);
    }

    @Override
    public NotificationEvent save(NotificationEvent event) {
        return toDomain(repo.save(toEntity(event)));
    }

    NotificationEvent toDomain(NotificationEventEntity e) {
        if (e == null) return null;
        return NotificationEvent.builder()
                .id(e.getId())
                .eventId(e.getEventId())
                .createdAt(e.getCreatedAt())
                .source(e.getSource())
                .eventType(e.getEventType())
                .entityId(e.getEntityId())
                .payload(e.getPayload())
                .build();
    }

    NotificationEventEntity toEntity(NotificationEvent d) {
        if (d == null) return null;
        return NotificationEventEntity.builder()
                .id(d.getId())
                .eventId(d.getEventId())
                .createdAt(d.getCreatedAt())
                .source(d.getSource())
                .eventType(d.getEventType())
                .entityId(d.getEntityId())
                .payload(d.getPayload())
                .build();
    }
}
