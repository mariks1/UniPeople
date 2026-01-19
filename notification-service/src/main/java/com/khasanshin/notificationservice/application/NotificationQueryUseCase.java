package com.khasanshin.notificationservice.application;

import com.khasanshin.notificationservice.dto.InboxItemDto;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationQueryUseCase {
    Page<InboxItemDto> inboxForUser(UUID me, Set<String> roles, Pageable pageable,
                                    Boolean unreadOnly, String source, String eventType,
                                    Instant from, Instant to);

    long unreadCountForUser(UUID me, Set<String> roles);

    Page<InboxItemDto> inboxByEmployee(UUID employeeId, Pageable pageable,
                                       Boolean unreadOnly, String source, String eventType,
                                       Instant from, Instant to);

    long unreadCount(UUID employeeId);

    void markAllRead(UUID employeeId, Instant now);

    void markAllReadForUser(UUID me, Set<String> roles, Instant now);

    void markReadSecured(UUID me, UUID inboxId, boolean isAdmin, Instant now);

    void deleteFromInboxSecured(UUID me, UUID inboxId, boolean isAdmin, Instant now);
}
