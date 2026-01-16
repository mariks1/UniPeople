package com.khasanshin.notificationservice.controller;

import com.khasanshin.notificationservice.config.PermissionGuard;
import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationQueryService service;
    private final PermissionGuard perm;

    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<InboxItemDto>> inbox(
            Pageable pageable,
            Authentication auth,
            @RequestParam(name = "unreadOnly", required = false) Boolean unreadOnly,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "eventType", required = false) String eventType,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to
    ) {
        UUID me = perm.employeeId(auth);
        var roles = perm.roles(auth);
        return ResponseEntity.ok(service.inboxForUser(me, roles, pageable, unreadOnly, source, eventType, from, to));
    }

    @GetMapping("/inbox/unread-count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> unreadCount(Authentication auth) {
        UUID me = perm.employeeId(auth);
        var roles = perm.roles(auth);
        return Map.of("count", service.unreadCountForUser(me, roles));
    }

    @PostMapping("/inbox/read-all")
    @PreAuthorize("isAuthenticated() and @perm.employeeId(authentication) != null")
    public ResponseEntity<Void> markAllRead(Authentication auth) {
        UUID me = perm.employeeId(auth);
        var roles = perm.roles(auth);
        service.markAllReadForUser(me, roles, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/inbox/{employeeId}")
    @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
    public ResponseEntity<Page<InboxItemDto>> inboxOf(
            @PathVariable("employeeId") UUID employeeId,
            Pageable pageable,
            @RequestParam(name = "unreadOnly", required = false) Boolean unreadOnly,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "eventType", required = false) String eventType,
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to
    ) {
        return ResponseEntity.ok(service.inboxByEmployee(employeeId, pageable, unreadOnly, source, eventType, from, to));
    }

    @GetMapping("/inbox/{employeeId}/unread-count")
    @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
    public Map<String, Long> unreadCountOf(@PathVariable("employeeId") UUID employeeId) {
        return Map.of("count", service.unreadCount(employeeId));
    }

    @PostMapping("/inbox/{employeeId}/read-all")
    @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
    public ResponseEntity<Void> markAllReadOf(@PathVariable("employeeId") UUID employeeId) {
        service.markAllRead(employeeId, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/inbox/{inboxId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markRead(@PathVariable("inboxId") UUID inboxId, Authentication auth) {
        UUID me = perm.employeeId(auth);
        boolean isAdmin = perm.hasAny(auth, "ORG_ADMIN", "HR");
        service.markReadSecured(me, inboxId, isAdmin, Instant.now());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/inbox/{inboxId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFromInbox(@PathVariable("inboxId") UUID inboxId, Authentication auth) {
        UUID me = perm.employeeId(auth);
        boolean isAdmin = perm.hasAny(auth, "ORG_ADMIN", "HR");
        service.deleteFromInboxSecured(me, inboxId, isAdmin, Instant.now());
        return ResponseEntity.noContent().build();
    }
}
