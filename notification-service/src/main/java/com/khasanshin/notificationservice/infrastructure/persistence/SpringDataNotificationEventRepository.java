package com.khasanshin.notificationservice.infrastructure.persistence;

import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationEventEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataNotificationEventRepository extends JpaRepository<NotificationEventEntity, UUID> {
    Optional<NotificationEventEntity> findByEventId(UUID eventId);
}
