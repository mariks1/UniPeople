package com.khasanshin.dutyservice.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationMessage(
        UUID eventId,
        String eventType,
        String source,
        Instant occurredAt,
        UUID entityId,
        Object payload,
        Recipients recipients
) {
    public record Recipients(
            Set<UUID> employeeIds,
            Set<String> roles
    ) {}
}
