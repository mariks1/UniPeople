package com.khasanshin.notificationservice.domain.port;

import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import java.util.Optional;
import java.util.UUID;

public interface NotificationEventRepositoryPort {
    Optional<NotificationEvent> findByEventId(UUID eventId);
    NotificationEvent save(NotificationEvent event);
}
