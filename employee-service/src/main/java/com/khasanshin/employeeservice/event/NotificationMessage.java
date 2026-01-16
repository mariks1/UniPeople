package com.khasanshin.common.events;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationMessage(
        UUID eventId,
        String eventType,      // "EMPLOYEE_CREATED"
        String source,         // "employee-service"
        Instant occurredAt,
        UUID entityId,         // employeeId / leaveId / ...
        Object payload,        // DTO/Map -> будет JSON объектом
        Recipients recipients  // кому доставить
) {
    public record Recipients(
            Set<UUID> employeeIds,  // персонально
            Set<String> roles       // shared по роли (HR/ORG_ADMIN)
    ) {}
}
