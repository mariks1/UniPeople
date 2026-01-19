package com.khasanshin.notificationservice.application;

import com.khasanshin.notificationservice.domain.port.NotificationInboxRepositoryPort;
import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.mapper.NotificationMapper;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationQueryApplicationService implements NotificationQueryUseCase {

    private final NotificationInboxRepositoryPort inboxRepo;
    private final NotificationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<InboxItemDto> inboxForUser(UUID me,
                                           Set<String> roles,
                                           Pageable pageable,
                                           Boolean unreadOnly,
                                           String source,
                                           String eventType,
                                           Instant from,
                                           Instant to) {
        Collection<String> roleList = roles == null ? Set.of() : roles;
        boolean hasMe = me != null;
        boolean hasRoles = roleList != null && !roleList.isEmpty();
        if (!hasMe && !hasRoles) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }

        Pageable p = normalize(pageable);
        return inboxRepo.findInboxForUser(me, roleList, unreadOnly, source, eventType, from, to, p)
                .map(mapper::toInboxDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCountForUser(UUID me, Set<String> roles) {
        Collection<String> roleList = roles == null ? Set.of() : roles;
        boolean rolesPresent = roleList != null && !roleList.isEmpty();

        if (me == null && !rolesPresent) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }

        return inboxRepo.countUnreadForUser(me, roleList, rolesPresent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InboxItemDto> inboxByEmployee(UUID employeeId,
                                              Pageable pageable,
                                              Boolean unreadOnly,
                                              String source,
                                              String eventType,
                                              Instant from,
                                              Instant to) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "employeeId is required");
        }
        Pageable p = normalize(pageable);
        return inboxRepo.findInboxByEmployee(employeeId, unreadOnly, source, eventType, from, to, p)
                .map(mapper::toInboxDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(UUID employeeId) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }
        return inboxRepo.countUnread(employeeId);
    }

    @Override
    @Transactional
    public void markAllRead(UUID employeeId, Instant now) {
        if (employeeId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No employeeId in token");
        }
        inboxRepo.markAllRead(employeeId, now);
    }

    @Override
    @Transactional
    public void markAllReadForUser(UUID me, Set<String> roles, Instant now) {
        Collection<String> roleList = roles == null ? Set.of() : roles;

        int updated = 0;
        if (me != null) updated += inboxRepo.markAllReadByEmployee(me, now);
        if (roleList != null && !roleList.isEmpty()) updated += inboxRepo.markAllReadByRoles(roleList, now);

        if (me == null && (roleList == null || roleList.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No recipient identity");
        }
    }

    @Override
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

    @Override
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
