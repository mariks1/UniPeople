package com.khasanshin.notificationservice.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NotificationInbox {
    UUID id;
    NotificationEvent event;
    UUID recipientEmployeeId;
    String recipientRole;
    Instant deliveredAt;
    Instant readAt;
    Instant deletedAt;
}
