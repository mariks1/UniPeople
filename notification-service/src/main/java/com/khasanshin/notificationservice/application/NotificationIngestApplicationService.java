package com.khasanshin.notificationservice.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import com.khasanshin.notificationservice.domain.model.NotificationInbox;
import com.khasanshin.notificationservice.domain.model.NotificationMessage;
import com.khasanshin.notificationservice.domain.port.NotificationEventRepositoryPort;
import com.khasanshin.notificationservice.domain.port.NotificationInboxRepositoryPort;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationIngestApplicationService implements NotificationIngestUseCase {

    private final NotificationEventRepositoryPort eventRepo;
    private final NotificationInboxRepositoryPort inboxRepo;
    private final ObjectMapper mapper;

    @Override
    @Transactional
    public void handle(NotificationMessage msg) {
        UUID eventId = msg.eventId();
        if (eventId == null) {
            throw new IllegalArgumentException("eventId is required");
        }

        String source = msg.source() == null ? "unknown" : msg.source();
        String eventType = msg.eventType() == null ? "unknown" : msg.eventType();
        UUID entityId = msg.entityId();
        Instant occurredAt = msg.occurredAt() == null ? Instant.now() : msg.occurredAt();

        String payload = toPayloadString(msg);

        NotificationEvent event = eventRepo.findByEventId(eventId)
                .orElseGet(() -> eventRepo.save(NotificationEvent.builder()
                        .eventId(eventId)
                        .createdAt(occurredAt)
                        .source(source)
                        .eventType(eventType)
                        .entityId(entityId)
                        .payload(payload)
                        .build()));

        Recipients recipients = resolveRecipients(msg);

        if (recipients.employeeIds.isEmpty() && recipients.roles.isEmpty()) {
            log.info("Event stored (no recipients). eventId={} type={} source={}", eventId, eventType, source);
            return;
        }

        Instant now = Instant.now();
        int delivered = 0;

        for (UUID empId : recipients.employeeIds) {
            try {
                inboxRepo.save(NotificationInbox.builder()
                        .event(event)
                        .recipientEmployeeId(empId)
                        .recipientRole(null)
                        .deliveredAt(now)
                        .readAt(null)
                        .deletedAt(null)
                        .build());
                delivered++;
            } catch (DataIntegrityViolationException dup) {
                // ignore duplicates for idempotency
            }
        }

        for (String role : recipients.roles) {
            try {
                inboxRepo.save(NotificationInbox.builder()
                        .event(event)
                        .recipientEmployeeId(null)
                        .recipientRole(role)
                        .deliveredAt(now)
                        .readAt(null)
                        .deletedAt(null)
                        .build());
                delivered++;
            } catch (DataIntegrityViolationException dup) {
                // ignore duplicates for idempotency
            }
        }

        log.info("Event stored and delivered. eventId={} type={} users={} roles={} inserted={}",
                eventId, eventType, recipients.employeeIds.size(), recipients.roles.size(), delivered);
    }

    private record Recipients(Set<UUID> employeeIds, Set<String> roles) {}

    private Recipients resolveRecipients(NotificationMessage msg) {
        Set<UUID> employeeIds = new HashSet<>();
        Set<String> roles = new HashSet<>();

        NotificationMessage.Recipients provided = msg.recipients();
        if (provided != null) {
            if (provided.employeeIds() != null) employeeIds.addAll(provided.employeeIds());
            if (provided.roles() != null) roles.addAll(provided.roles());
        }

        if (employeeIds.isEmpty() && roles.isEmpty()) {
            // fallback by eventType/payload to satisfy legacy behaviour/tests
            try {
                JsonNode root = mapper.valueToTree(msg);
                JsonNode payload = root.path("payload");

                switch (msg.eventType()) {
                    case "EMPLOYEE_CREATED" -> {
                        roles.add("HR");
                        roles.add("ORG_ADMIN");
                    }
                    case "LEAVE_CREATED" -> {
                        roles.add("HR");
                        roles.add("DEPT_HEAD");
                        UUID emp = parseUuid(payload.path("employeeId").asText(null));
                        if (emp != null) employeeIds.add(emp);
                    }
                    case "LEAVE_APPROVED" -> {
                        UUID emp = parseUuid(payload.path("employeeId").asText(null));
                        UUID approver = parseUuid(payload.path("approverId").asText(null));
                        if (emp != null) employeeIds.add(emp);
                        if (approver != null) employeeIds.add(approver);
                    }
                    default -> { }
                }
            } catch (Exception ignored) {
                // keep recipients empty if parsing fails
            }
        }

        return new Recipients(employeeIds, roles);
    }

    private String toPayloadString(NotificationMessage msg) {
        try {
            return mapper.writeValueAsString(msg);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize notification payload", e);
        }
    }

    private UUID parseUuid(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
