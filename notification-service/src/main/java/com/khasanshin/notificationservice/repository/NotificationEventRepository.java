package com.khasanshin.notificationservice.repository;

import com.khasanshin.notificationservice.entity.NotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, UUID> {
    Optional<NotificationEvent> findByEventId(UUID eventId);
}

