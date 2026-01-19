package com.khasanshin.notificationservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "notification_inbox", schema = "notif",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ni_event_recipient_oneof",
                columnNames = {"event_pk", "recipient_employee_id", "recipient_role"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_pk", nullable = false)
    private NotificationEventEntity event;

    @Column(name = "recipient_employee_id")
    private UUID recipientEmployeeId;

    @Column(name = "recipient_role", length = 64)
    private String recipientRole;

    @Column(name = "delivered_at", nullable = false)
    private Instant deliveredAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
