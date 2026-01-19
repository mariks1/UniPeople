package com.khasanshin.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.config.PermissionGuard;
import com.khasanshin.notificationservice.infrastructure.persistence.SpringDataNotificationInboxRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "kafka.topics.employee=employee.events",
        "kafka.topics.leave=leave.events",
        "kafka.topics.duty=duty.events",
        "kafka.topics.dlq=notification.dlq",

        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:65535/jwks"
})
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class NotificationE2EIT {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:16");
    private static final DockerImageName KAFKA_IMAGE =
            DockerImageName.parse("apache/kafka:3.7.0");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE)
            .withDatabaseName("unipeople")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static KafkaContainer kafka = new KafkaContainer(KAFKA_IMAGE);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        Startables.deepStart(Stream.of(postgres, kafka)).join();
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "notification-it");
    }

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    SpringDataNotificationInboxRepository inboxRepo;

    @Autowired
    ObjectMapper mapper;

    @Test
    @Transactional
    void consumesKafkaEvent_andPersistsInbox() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID employeeId = UUID.randomUUID();

        Map<String, Object> payload = Map.of(
                "eventId", eventId.toString(),
                "eventType", "EMPLOYEE_CREATED",
                "source", "employee-service",
                "occurredAt", "2026-01-15T10:00:00Z",
                "entityId", employeeId.toString(),
                "payload", Map.of(
                        "id", employeeId.toString(),
                        "first_name", "Ada",
                        "last_name", "Lovelace"
                )
        );

        String json = mapper.writeValueAsString(payload);
        kafkaTemplate.send(new ProducerRecord<>("employee.events", eventId.toString(), json)).get();

        long start = System.currentTimeMillis();
        long found = 0;
        while (System.currentTimeMillis() - start < Duration.ofSeconds(15).toMillis()) {
            found = inboxRepo.count();
            if (found > 0) break;
            Thread.sleep(300);
        }

        assertThat(found).isGreaterThan(0);
        assertThat(inboxRepo.findAll()).anySatisfy(inbox ->
                assertThat(inbox.getEvent().getEventId()).isEqualTo(eventId)
        );
    }
}
