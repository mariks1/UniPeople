package com.khasanshin.notificationservice.service;

import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.dto.NotificationEventDto;
import com.khasanshin.notificationservice.entity.NotificationEvent;
import com.khasanshin.notificationservice.entity.NotificationInbox;
import com.khasanshin.notificationservice.mapper.NotificationViewMapper;
import com.khasanshin.notificationservice.repository.NotificationInboxRepository;
import com.khasanshin.notificationservice.repository.NotificationInboxSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final NotificationInboxRepository inboxRepo;
    private final NotificationViewMapper viewMapper;

    @Transactional(readOnly = true)
    public Page<InboxItemDto> inboxForUser(
            UUID me,
            Set<String> roles,
            Pageable pageable,
            Boolean unreadOnly,
            String source,
            String eventType,
            Instant from,
            Instant to
    ) {

        Collection<String> roleList = (roles == null) ? java.util.List.of() : roles;

        boolean hasMe = me != null;
        boolean hasRoles = roleList != null && !roleList.isEmpty();
        if (!hasMe && !hasRoles) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }

        Pageable p = normalize(pageable);

        var spec = NotificationInboxSpecs.inboxForUser(me, roleList, unreadOnly, source, eventType, from, to);
        return inboxRepo.findAll(spec, p).map(this::toInboxDto);

    }

    @Transactional(readOnly = true)
    public long unreadCountForUser(UUID me, Set<String> roles) {
        Collection<String> roleList = (roles == null) ? java.util.List.of() : roles;
        boolean rolesPresent = !roleList.isEmpty();

        if (me == null && !rolesPresent) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }

        return inboxRepo.countUnreadForUser(me, roleList, rolesPresent);
    }

    @Transactional(readOnly = true)
    public Page<InboxItemDto> inboxByEmployee(
            UUID employeeId,
            Pageable pageable,
            Boolean unreadOnly,
            String source,
            String eventType,
            Instant from,
            Instant to
    ) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeId is required");
        }
        Pageable p = normalize(pageable);
        return inboxRepo.findInboxByEmployee(employeeId, unreadOnly, source, eventType, from, to, p)
                .map(this::toInboxDto);
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID employeeId) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }
        return inboxRepo.countByRecipientEmployeeIdAndReadAtIsNullAndDeletedAtIsNull(employeeId);
    }

    @Transactional
    public void markAllRead(UUID employeeId, Instant now) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }
        inboxRepo.markAllRead(employeeId, now);
    }

    @Transactional
    public void markAllReadForUser(UUID me, Set<String> roles, Instant now) {
        List<String> roleList = (roles == null) ? List.of() : new ArrayList<>(roles);

        int updated = 0;
        if (me != null) updated += inboxRepo.markAllReadByEmployee(me, now);
        if (!roleList.isEmpty()) updated += inboxRepo.markAllReadByRoles(roleList, now);

        if (me == null && roleList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }
    }

    @Transactional
    public void markReadSecured(UUID me, UUID inboxId, boolean isAdmin, Instant now) {
        if (me == null && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }

        int updated = isAdmin
                ? inboxRepo.markReadAdmin(inboxId, now)
                : inboxRepo.markRead(inboxId, me, now);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inbox item not found");
        }
    }

    @Transactional
    public void deleteFromInboxSecured(UUID me, UUID inboxId, boolean isAdmin, Instant now) {
        if (me == null && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }

        int updated = isAdmin
                ? inboxRepo.softDeleteAdmin(inboxId, now)
                : inboxRepo.softDelete(inboxId, me, now);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inbox item not found");
        }
    }

    private InboxItemDto toInboxDto(NotificationInbox i) {
        NotificationEvent e = i.getEvent();

        NotificationEventDto eventDto = new NotificationEventDto(
                e.getEventId(),
                e.getCreatedAt(),
                e.getSource(),
                e.getEventType(),
                e.getEntityId(),
                viewMapper.title(e),
                e.getPayload()
        );

        return new InboxItemDto(
                i.getId(),
                i.getReadAt() == null,
                i.getDeliveredAt(),
                eventDto
        );
    }

    private Pageable normalize(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), 50);

        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Order.desc("deliveredAt"));
        }
        return PageRequest.of(page, size, sort);
    }
}
