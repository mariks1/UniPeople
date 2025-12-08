package com.khasanshin.fileservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.fileservice.dto.event.FileDeletedEvent;
import com.khasanshin.fileservice.dto.event.FileUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "file.events";

    public void publishFileUploaded(FileUploadedEvent event) {
        send(TOPIC, event.getOwnerId().toString(), event);
    }

    public void publishFileDeleted(FileDeletedEvent event) {
        send(TOPIC, event.getOwnerId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, key, json);
            log.info("Sent Kafka event to {}: {}", topic, json);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Kafka event", e);
        }
    }
}
