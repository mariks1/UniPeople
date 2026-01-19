package com.khasanshin.notificationservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notification_event", schema = "notif",
        uniqueConstraints = @UniqueConstraint(name = "uk_ne_event_id", columnNames = "event_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private String payload;
}
