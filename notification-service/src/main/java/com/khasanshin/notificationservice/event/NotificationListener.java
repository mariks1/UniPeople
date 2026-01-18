package com.khasanshin.notificationservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.entity.NotificationEvent;
import com.khasanshin.notificationservice.entity.NotificationInbox;
import com.khasanshin.notificationservice.repository.NotificationEventRepository;
import com.khasanshin.notificationservice.repository.NotificationInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationEventRepository eventRepo;
    private final NotificationInboxRepository inboxRepo;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "${kafka.topics.employee}")
    public void onEmployee(String json) throws Exception { handle(json); }

    @KafkaListener(topics = "${kafka.topics.leave}")
    public void onLeave(String json) throws Exception { handle(json); }

    @KafkaListener(topics = "${kafka.topics.duty}")
    public void onDuty(String json) throws Exception { handle(json); }

    @Transactional
    public void handle(String json) throws Exception {
        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);

        UUID eventId = msg.eventId();
        String source = msg.source() == null ? "unknown" : msg.source();
        String eventType = msg.eventType() == null ? "unknown" : msg.eventType();
        UUID entityId = msg.entityId();
        Instant occurredAt = msg.occurredAt() == null ? Instant.now() : msg.occurredAt();

        NotificationEvent event = eventRepo.findByEventId(eventId)
                .orElseGet(() -> eventRepo.save(NotificationEvent.builder()
                        .eventId(eventId)
                        .createdAt(occurredAt)
                        .source(source)
                        .eventType(eventType)
                        .entityId(entityId)
                        .payload(json)
                        .build()));

        var r = msg.recipients();
        Set<UUID> employeeIds = (r == null || r.employeeIds() == null) ? Set.of() : r.employeeIds();
        Set<String> roles = (r == null || r.roles() == null) ? Set.of() : r.roles();

        if (employeeIds.isEmpty() && roles.isEmpty()) {
            log.info("Event stored (no recipients). eventId={} type={} source={}", eventId, eventType, source);
            return;
        }

        Instant now = Instant.now();
        int delivered = 0;

        for (UUID empId : employeeIds) {
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
                //
            }
        }

        for (String role : roles) {
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
                //
            }
        }

        log.info("Event stored and delivered. eventId={} type={} users={} roles={} inserted={}",
                eventId, eventType, employeeIds.size(), roles.size(), delivered);
    }
}
