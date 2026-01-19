package com.khasanshin.notificationservice.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationViewMapper {

    private final ObjectMapper mapper;

    @Named("title")
    public String title(NotificationEvent e) {
        try {
            JsonNode root = mapper.readTree(e.getPayload());
            JsonNode p = root.path("payload");

            return switch (e.getEventType()) {
                case "EMPLOYEE_CREATED" ->
                        "New employee: " + p.path("first_name").asText("") + " " + p.path("last_name").asText("");
                case "LEAVE_CREATED" -> "Leave request created";
                case "LEAVE_APPROVED" -> "Leave approved";
                case "DUTY_ASSIGNED" -> "Duty assigned";
                default -> e.getEventType();
            };
        } catch (Exception ex) {
            return e.getEventType();
        }
    }
}
