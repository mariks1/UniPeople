package com.khasanshin.notificationservice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.application.NotificationIngestUseCase;
import com.khasanshin.notificationservice.domain.model.NotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationIngestUseCase ingest;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "${kafka.topics.employee}")
    public void onEmployee(String json) throws Exception { handle(json); }

    @KafkaListener(topics = "${kafka.topics.leave}")
    public void onLeave(String json) throws Exception { handle(json); }

    @KafkaListener(topics = "${kafka.topics.duty}")
    public void onDuty(String json) throws Exception { handle(json); }

    public void handle(String json) {
        try {
            NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
            ingest.handle(msg);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid notification payload", ex);
        }
    }
}
