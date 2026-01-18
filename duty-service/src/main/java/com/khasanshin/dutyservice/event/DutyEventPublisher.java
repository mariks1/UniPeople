package com.khasanshin.dutyservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DutyEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.duty:duty.events}")
    private String topic;

    private static final String SOURCE = "duty-service";
    private static final Set<String> HR_ROLES = Set.of("HR", "ORG_ADMIN");

    public void dutyCreated(DutyDto dto) {
        send("DUTY_CREATED", dto.getId(), dto, recipients(null, HR_ROLES));
    }

    public void dutyUpdated(DutyDto dto) {
        send("DUTY_UPDATED", dto.getId(), dto, recipients(null, HR_ROLES));
    }

    public void dutyDeleted(UUID dutyId) {
        send("DUTY_DELETED", dutyId, java.util.Map.of("id", dutyId.toString()), recipients(null, HR_ROLES));
    }

    public void dutyAssigned(DutyAssignmentDto dto) {
        Set<UUID> employeeIds = dto.getEmployeeId() == null ? Set.of() : Set.of(dto.getEmployeeId());
        send("DUTY_ASSIGNED", dto.getId(), dto, recipients(employeeIds, HR_ROLES));
    }

    public void dutyUnassigned(UUID assignmentId, UUID employeeId, UUID departmentId, UUID dutyId) {
        Set<UUID> employeeIds = employeeId == null ? Set.of() : Set.of(employeeId);

        var payload = java.util.Map.of(
                "assignmentId", assignmentId.toString(),
                "employeeId", employeeId == null ? null : employeeId.toString(),
                "departmentId", departmentId == null ? null : departmentId.toString(),
                "dutyId", dutyId == null ? null : dutyId.toString()
        );

        send("DUTY_UNASSIGNED", assignmentId, payload, recipients(employeeIds, HR_ROLES));
    }

    private NotificationMessage.Recipients recipients(Set<UUID> employeeIds, Set<String> roles) {
        return new NotificationMessage.Recipients(
                employeeIds == null ? Set.of() : employeeIds,
                roles == null ? Set.of() : roles
        );
    }

    private void send(String eventType, UUID entityId, Object payload, NotificationMessage.Recipients recipients) {
        NotificationMessage msg = new NotificationMessage(
                UUID.randomUUID(),
                eventType,
                SOURCE,
                Instant.now(),
                entityId,
                payload,
                recipients
        );

        try {
            String json = objectMapper.writeValueAsString(msg);
            kafka.send(topic, msg.eventId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + eventType, e);
        }
    }
}
