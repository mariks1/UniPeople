package com.khasanshin.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationEventDto(
        UUID eventId,
        Instant createdAt,
        String source,
        String eventType,
        UUID entityId,
        String title,
        String payload
) {}

