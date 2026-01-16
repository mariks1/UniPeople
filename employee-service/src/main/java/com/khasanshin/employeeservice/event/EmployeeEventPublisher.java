package com.khasanshin.employeeservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmployeeEventPublisher {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.employee:employee.events}")
    private String topic;

    private static final String SOURCE = "employee-service";
    private static final Set<String> ROLES = Set.of("HR", "ORG_ADMIN");

    public void publishEmployeeEvent(String eventType, UUID employeeId, Object payload) {
        NotificationMessage msg = new NotificationMessage(
                UUID.randomUUID(),
                eventType,
                SOURCE,
                Instant.now(),
                employeeId,
                payload,
                new NotificationMessage.Recipients(
                        Set.of(employeeId),
                        ROLES
                )
        );

        try {
            String json = objectMapper.writeValueAsString(msg);
            kafka.send(topic, msg.eventId().toString(), json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event " + eventType, e);
        }
    }
}
