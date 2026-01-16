package com.khasanshin.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="notification_inbox", schema="notif",
        uniqueConstraints = @UniqueConstraint(
                name="uk_ni_event_recipient_oneof",
                columnNames={"event_pk","recipient_employee_id","recipient_role"} // для схемы, индекс всё равно надёжнее
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="event_pk", nullable=false)
    private NotificationEvent event;

    @Column(name="recipient_employee_id")
    private UUID recipientEmployeeId;

    @Column(name="recipient_role", length = 64)
    private String recipientRole;

    @Column(name="delivered_at", nullable=false)
    private Instant deliveredAt;

    @Column(name="read_at")
    private Instant readAt;

    @Column(name="deleted_at")
    private Instant deletedAt;
}
