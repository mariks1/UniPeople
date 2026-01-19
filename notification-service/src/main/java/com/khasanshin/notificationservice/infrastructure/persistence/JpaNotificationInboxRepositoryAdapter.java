package com.khasanshin.notificationservice.infrastructure.persistence;

import com.khasanshin.notificationservice.domain.model.NotificationInbox;
import com.khasanshin.notificationservice.domain.port.NotificationInboxRepositoryPort;
import com.khasanshin.notificationservice.infrastructure.persistence.entity.NotificationInboxEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaNotificationInboxRepositoryAdapter implements NotificationInboxRepositoryPort {

    private final SpringDataNotificationInboxRepository repo;
    private final JpaNotificationEventRepositoryAdapter eventMapper;

    @Override
    public NotificationInbox save(NotificationInbox inbox) {
        return toDomain(repo.save(toEntity(inbox)));
    }

    @Override
    public Page<NotificationInbox> findInboxForUser(UUID me, Collection<String> roles, Boolean unreadOnly, String source, String eventType, Instant from, Instant to, Pageable pageable) {
        return repo.findAll(NotificationInboxSpecs.inboxForUser(me, roles, unreadOnly, source, eventType, from, to), pageable)
                .map(this::toDomain);
    }

    @Override
    public long countUnreadForUser(UUID me, Collection<String> roles, boolean rolesPresent) {
        return repo.countUnreadForUser(me, roles, rolesPresent);
    }

    @Override
    public Page<NotificationInbox> findInboxByEmployee(UUID employeeId, Boolean unreadOnly, String source, String eventType, Instant from, Instant to, Pageable pageable) {
        return repo.findInboxByEmployee(employeeId, unreadOnly, source, eventType, from, to, pageable)
                .map(this::toDomain);
    }

    @Override
    public long countUnread(UUID employeeId) {
        return repo.countByRecipientEmployeeIdAndReadAtIsNullAndDeletedAtIsNull(employeeId);
    }

    @Override
    public int markAllRead(UUID employeeId, Instant now) {
        return repo.markAllRead(employeeId, now);
    }

    @Override
    public int markAllReadByEmployee(UUID employeeId, Instant now) {
        return repo.markAllReadByEmployee(employeeId, now);
    }

    @Override
    public int markAllReadByRoles(Collection<String> roles, Instant now) {
        return repo.markAllReadByRoles(roles, now);
    }

    @Override
    public int markRead(UUID id, UUID me, Instant now) {
        return repo.markRead(id, me, now);
    }

    @Override
    public int markReadAdmin(UUID id, Instant now) {
        return repo.markReadAdmin(id, now);
    }

    @Override
    public int softDelete(UUID id, UUID me, Instant now) {
        return repo.softDelete(id, me, now);
    }

    @Override
    public int softDeleteAdmin(UUID id, Instant now) {
        return repo.softDeleteAdmin(id, now);
    }

    private NotificationInbox toDomain(NotificationInboxEntity e) {
        if (e == null) return null;
        return NotificationInbox.builder()
                .id(e.getId())
                .event(eventMapper.toDomain(e.getEvent()))
                .recipientEmployeeId(e.getRecipientEmployeeId())
                .recipientRole(e.getRecipientRole())
                .deliveredAt(e.getDeliveredAt())
                .readAt(e.getReadAt())
                .deletedAt(e.getDeletedAt())
                .build();
    }

    private NotificationInboxEntity toEntity(NotificationInbox d) {
        if (d == null) return null;
        return NotificationInboxEntity.builder()
                .id(d.getId())
                .event(eventMapper.toEntity(d.getEvent()))
                .recipientEmployeeId(d.getRecipientEmployeeId())
                .recipientRole(d.getRecipientRole())
                .deliveredAt(d.getDeliveredAt())
                .readAt(d.getReadAt())
                .deletedAt(d.getDeletedAt())
                .build();
    }
}
