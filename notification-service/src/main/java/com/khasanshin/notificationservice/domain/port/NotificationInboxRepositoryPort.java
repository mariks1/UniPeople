package com.khasanshin.notificationservice.domain.port;

import com.khasanshin.notificationservice.domain.model.NotificationInbox;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationInboxRepositoryPort {

    NotificationInbox save(NotificationInbox inbox);

    Page<NotificationInbox> findInboxForUser(UUID me,
                                             Collection<String> roles,
                                             Boolean unreadOnly,
                                             String source,
                                             String eventType,
                                             Instant from,
                                             Instant to,
                                             Pageable pageable);

    long countUnreadForUser(UUID me, Collection<String> roles, boolean rolesPresent);

    Page<NotificationInbox> findInboxByEmployee(UUID employeeId,
                                                Boolean unreadOnly,
                                                String source,
                                                String eventType,
                                                Instant from,
                                                Instant to,
                                                Pageable pageable);

    long countUnread(UUID employeeId);

    int markAllRead(UUID employeeId, Instant now);

    int markAllReadByEmployee(UUID employeeId, Instant now);

    int markAllReadByRoles(Collection<String> roles, Instant now);

    int markRead(UUID id, UUID me, Instant now);

    int markReadAdmin(UUID id, Instant now);

    int softDelete(UUID id, UUID me, Instant now);

    int softDeleteAdmin(UUID id, Instant now);
}
