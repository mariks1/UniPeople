package com.khasanshin.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.khasanshin.notificationservice.application.NotificationIngestApplicationService;
import com.khasanshin.notificationservice.domain.model.NotificationEvent;
import com.khasanshin.notificationservice.domain.model.NotificationInbox;
import com.khasanshin.notificationservice.domain.model.NotificationMessage;
import com.khasanshin.notificationservice.domain.port.NotificationEventRepositoryPort;
import com.khasanshin.notificationservice.domain.port.NotificationInboxRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationListenerTest {

    @Mock NotificationEventRepositoryPort eventRepo;
    @Mock NotificationInboxRepositoryPort inboxRepo;

    ObjectMapper mapper = new ObjectMapper();
    NotificationIngestApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper.registerModule(new JavaTimeModule());
        service = new NotificationIngestApplicationService(eventRepo, inboxRepo, mapper);
    }

    @Test
    void handle_employeeCreated_storesEvent_andDeliversToRoles_HR_and_ORG_ADMIN() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        String json = """
        {
          "eventId":"%s",
          "eventType":"EMPLOYEE_CREATED",
          "source":"employee-service",
          "occurredAt":"2026-01-15T10:00:00Z",
          "entityId":"%s",
          "payload":{"id":"%s","first_name":"A","last_name":"B"}
        }
        """.formatted(eventId, entityId, entityId);

        when(eventRepo.findByEventId(eventId)).thenReturn(Optional.empty());
        when(eventRepo.save(any(NotificationEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
        service.handle(msg);

        verify(eventRepo).save(any(NotificationEvent.class));

        ArgumentCaptor<NotificationInbox> captor = ArgumentCaptor.forClass(NotificationInbox.class);
        verify(inboxRepo, times(2)).save(captor.capture());

        List<NotificationInbox> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(i -> "HR".equals(i.getRecipientRole())));
        assertTrue(saved.stream().anyMatch(i -> "ORG_ADMIN".equals(i.getRecipientRole())));

        assertTrue(saved.stream().allMatch(i -> i.getRecipientEmployeeId() == null));
    }

    @Test
    void handle_leaveCreated_deliversToRoles_HR_DEPT_HEAD_andEmployee() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();

        String json = """
        {
          "eventId":"%s",
          "eventType":"LEAVE_CREATED",
          "source":"leave-service",
          "occurredAt":"2026-01-15T10:00:00Z",
          "payload":{"employeeId":"%s","approverId":"%s"}
        }
        """.formatted(eventId, empId, approverId);

        when(eventRepo.findByEventId(eventId)).thenReturn(Optional.empty());
        when(eventRepo.save(any(NotificationEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
        service.handle(msg);

        ArgumentCaptor<NotificationInbox> captor = ArgumentCaptor.forClass(NotificationInbox.class);
        verify(inboxRepo, times(3)).save(captor.capture());

        List<NotificationInbox> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(i -> empId.equals(i.getRecipientEmployeeId())));
        assertTrue(saved.stream().anyMatch(i -> "HR".equals(i.getRecipientRole())));
        assertTrue(saved.stream().anyMatch(i -> "DEPT_HEAD".equals(i.getRecipientRole())));
    }

    @Test
    void handle_leaveApproved_deliversToEmployeeAndApprover() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID empId = UUID.randomUUID();
        UUID approverId = UUID.randomUUID();

        String json = """
        {
          "eventId":"%s",
          "eventType":"LEAVE_APPROVED",
          "source":"leave-service",
          "occurredAt":"2026-01-15T10:00:00Z",
          "payload":{"employeeId":"%s","approverId":"%s"}
        }
        """.formatted(eventId, empId, approverId);

        when(eventRepo.findByEventId(eventId)).thenReturn(Optional.empty());
        when(eventRepo.save(any(NotificationEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
        service.handle(msg);

        ArgumentCaptor<NotificationInbox> captor = ArgumentCaptor.forClass(NotificationInbox.class);
        verify(inboxRepo, times(2)).save(captor.capture());

        List<NotificationInbox> saved = captor.getAllValues();
        assertTrue(saved.stream().anyMatch(i -> empId.equals(i.getRecipientEmployeeId())));
        assertTrue(saved.stream().anyMatch(i -> approverId.equals(i.getRecipientEmployeeId())));
    }

    @Test
    void handle_unknownEvent_noRecipients_onlyStoresEvent() throws Exception {
        UUID eventId = UUID.randomUUID();

        String json = """
        {
          "eventId":"%s",
          "eventType":"SOMETHING",
          "source":"x",
          "occurredAt":"2026-01-15T10:00:00Z",
          "payload":{}
        }
        """.formatted(eventId);

        when(eventRepo.findByEventId(eventId)).thenReturn(Optional.empty());
        when(eventRepo.save(any(NotificationEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
        service.handle(msg);

        verify(eventRepo).save(any(NotificationEvent.class));
        verify(inboxRepo, never()).save(any());
    }

    @Test
    void handle_ignoresDuplicateInboxRows_andContinues() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        String json = """
        {
          "eventId":"%s",
          "eventType":"EMPLOYEE_CREATED",
          "source":"employee-service",
          "occurredAt":"2026-01-15T10:00:00Z",
          "entityId":"%s",
          "payload":{"id":"%s"}
        }
        """.formatted(eventId, entityId, entityId);

        when(eventRepo.findByEventId(eventId)).thenReturn(Optional.empty());
        when(eventRepo.save(any(NotificationEvent.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);

        NotificationInbox stored = NotificationInbox.builder()
                .id(UUID.randomUUID())
                .event(NotificationEvent.builder().eventId(eventId).createdAt(java.time.Instant.now()).source("employee-service").eventType("EMPLOYEE_CREATED").payload("{}").build())
                .deliveredAt(java.time.Instant.now())
                .build();

        when(inboxRepo.save(any(NotificationInbox.class)))
                .thenThrow(new DataIntegrityViolationException("dup"))
                .thenReturn(stored);

        assertDoesNotThrow(() -> service.handle(msg));
        verify(inboxRepo, times(2)).save(any(NotificationInbox.class));
    }

    @Test
    void handle_throwsOnInvalidEventId() throws Exception {
        String json = """
        {"eventType":"X","payload":{}}
        """;
        NotificationMessage msg = mapper.readValue(json, NotificationMessage.class);
        assertThrows(IllegalArgumentException.class, () -> service.handle(msg));
    }
}
