package com.khasanshin.leaveservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LeaveEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    @Value("${kafka.topics.leave:leave.events}")
    private String topic;

    private static final String SOURCE = "leave-service";
    private static final Set<String> HR_ROLES = Set.of("HR", "ORG_ADMIN");

    public void leaveCreated(UUID leaveId, Object dto) {
        Recipients r = recipientsForLeave(dto, true);
        send("LEAVE_CREATED", leaveId, dto, r);
    }

    public void leaveApproved(UUID leaveId, Object dto) {
        Recipients r = recipientsForLeave(dto, false);
        send("LEAVE_APPROVED", leaveId, dto, r);
    }

    public void leaveRejected(UUID leaveId, Object dto) {
        Recipients r = recipientsForLeave(dto, false);
        send("LEAVE_REJECTED", leaveId, dto, r);
    }

    public void leaveCanceled(UUID leaveId, Object dto) {
        Recipients r = recipientsForLeave(dto, true);
        send("LEAVE_CANCELED", leaveId, dto, r);
    }

    private void send(String eventType, UUID entityId, Object payload, Recipients recipients) {
        NotificationMessage msg = new NotificationMessage(
                UUID.randomUUID(),
                eventType,
                SOURCE,
                Instant.now(),
                entityId,
                payload,
                new NotificationMessage.Recipients(recipients.employeeIds, recipients.roles)
        );

        try {
            String json = mapper.writeValueAsString(msg);
            kafka.send(topic, msg.eventId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + eventType, e);
        }
    }

    private Recipients recipientsForLeave(Object dto, boolean includeApprover) {
        JsonNode n = mapper.valueToTree(dto);

        UUID employeeId = firstUuid(n,
                "employeeId", "employee_id", "employee", "employeePk", "employee_pk"
        );

        UUID approverId = includeApprover
                ? firstUuid(n, "approverId", "approver_id", "approverEmployeeId", "approver_employee_id")
                : null;

        Set<UUID> employeeIds = new LinkedHashSet<>();
        if (employeeId != null) employeeIds.add(employeeId);
        if (approverId != null) employeeIds.add(approverId);

        return new Recipients(employeeIds, HR_ROLES);
    }

    private UUID firstUuid(JsonNode n, String... fields) {
        for (String f : fields) {
            JsonNode v = n.get(f);
            if (v != null && !v.isNull()) {
                UUID u = uuidOrNull(v.asText());
                if (u != null) return u;
            }
        }
        return null;
    }

    private UUID uuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    private record Recipients(Set<UUID> employeeIds, Set<String> roles) {}
}
