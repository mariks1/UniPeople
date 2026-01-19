package com.khasanshin.notificationservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NotificationEvent {
    UUID id;
    UUID eventId;
    Instant createdAt;
    String source;
    String eventType;
    UUID entityId;
    String payload;
}
