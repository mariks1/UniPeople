package com.khasanshin.notificationservice.dto;

import java.time.Instant;
import java.util.UUID;

public record InboxItemDto(
        UUID inboxId,
        boolean unread,
        Instant deliveredAt,
        NotificationEventDto event
) {}