package com.khasanshin.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.notificationservice.config.PermissionGuard;
import com.khasanshin.notificationservice.controller.NotificationController;
import com.khasanshin.notificationservice.dto.InboxItemDto;
import com.khasanshin.notificationservice.dto.NotificationEventDto;
import com.khasanshin.notificationservice.service.NotificationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = NotificationController.class)
@Import({PermissionGuard.class, TestSecurityConfig.class})
class NotificationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean NotificationQueryService service;
    @MockitoBean JwtDecoder jwtDecoder;

    private static RequestPostProcessor asEmployee(UUID employeeId) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
                .jwt(jwt -> jwt
                        .claim("roles", List.of("EMPLOYEE"))
                        .claim("employeeId", employeeId.toString())
                );
    }

    private static RequestPostProcessor asHr(UUID employeeId) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_HR"))
                .jwt(jwt -> jwt
                        .claim("roles", List.of("HR"))
                        .claim("employeeId", employeeId.toString())
                );
    }

    private static RequestPostProcessor asRoleOnly(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_" + role))
                .jwt(jwt -> jwt.claim("roles", List.of(role)));
    }

    private static InboxItemDto sampleInboxItem() {
        UUID eventId = UUID.randomUUID();
        UUID inboxId = UUID.randomUUID();

        NotificationEventDto e = new NotificationEventDto(
                eventId,
                Instant.parse("2026-01-15T10:00:00Z"),
                "employee-service",
                "EMPLOYEE_CREATED",
                UUID.randomUUID(),
                "New employee: John Doe",
                "{\"x\":1}"
        );

        return new InboxItemDto(inboxId, true, Instant.parse("2026-01-15T10:01:00Z"), e);
    }

    @Test
    void inbox_200_employee() throws Exception {
        UUID me = UUID.randomUUID();

        var item = sampleInboxItem();
        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

        when(service.inboxForUser(eq(me), anySet(), any(Pageable.class), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/notifications/inbox")
                        .param("page","0").param("size","20")
                        .with(asEmployee(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].inboxId").value(item.inboxId().toString()))
                .andExpect(jsonPath("$.content[0].unread").value(true));

        verify(service).inboxForUser(eq(me),
                argThat(s -> s.contains("EMPLOYEE")),
                any(Pageable.class),
                any(), any(), any(), any(), any());
    }

    @Test
    void inbox_200_roleOnly_withoutEmployeeId() throws Exception {
        var empty = new PageImpl<InboxItemDto>(List.of(), PageRequest.of(0, 20), 0);

        when(service.inboxForUser(isNull(), anySet(), any(Pageable.class), any(), any(), any(), any(), any()))
                .thenReturn(empty);

        mvc.perform(get("/api/v1/notifications/inbox")
                        .with(asRoleOnly("HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(service).inboxForUser(isNull(),
                argThat(s -> s.contains("HR")),
                any(Pageable.class),
                any(), any(), any(), any(), any());
    }

    @Test
    void inboxOf_200_hr() throws Exception {
        UUID hrEmpId = UUID.randomUUID();
        UUID targetEmployeeId = UUID.randomUUID();

        var item = sampleInboxItem();
        var page = new PageImpl<>(List.of(item), PageRequest.of(0, 20), 1);

        when(service.inboxByEmployee(eq(targetEmployeeId), any(Pageable.class), any(), any(), any(), any(), any()))
                .thenReturn(page);

        mvc.perform(get("/api/v1/notifications/inbox/{employeeId}", targetEmployeeId)
                        .param("page","0").param("size","20")
                        .with(asHr(hrEmpId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service).inboxByEmployee(eq(targetEmployeeId),
                any(Pageable.class),
                any(), any(), any(), any(), any());
    }

    @Test
    void inboxOf_403_employee() throws Exception {
        UUID me = UUID.randomUUID();
        UUID targetEmployeeId = UUID.randomUUID();

        mvc.perform(get("/api/v1/notifications/inbox/{employeeId}", targetEmployeeId)
                        .with(asEmployee(me)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void unreadCount_200_employee() throws Exception {
        UUID me = UUID.randomUUID();
        when(service.unreadCountForUser(eq(me), anySet())).thenReturn(7L);

        mvc.perform(get("/api/v1/notifications/inbox/unread-count")
                        .with(asEmployee(me)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(7));

        verify(service).unreadCountForUser(eq(me), argThat(s -> s.contains("EMPLOYEE")));
    }

    @Test
    void markAllRead_204_employee() throws Exception {
        UUID me = UUID.randomUUID();

        mvc.perform(post("/api/v1/notifications/inbox/read-all")
                        .with(asEmployee(me)))
                .andExpect(status().isNoContent());

        verify(service).markAllRead(eq(me), any(Instant.class));
    }

    @Test
    void markRead_204_employee_callsSecuredWithIsAdminFalse() throws Exception {
        UUID me = UUID.randomUUID();
        UUID inboxId = UUID.randomUUID();

        mvc.perform(post("/api/v1/notifications/inbox/{inboxId}/read", inboxId)
                        .with(asEmployee(me)))
                .andExpect(status().isNoContent());

        verify(service).markReadSecured(eq(me), eq(inboxId), eq(false), any(Instant.class));
    }

    @Test
    void markRead_204_hr_callsSecuredWithIsAdminTrue() throws Exception {
        UUID hrEmpId = UUID.randomUUID();
        UUID inboxId = UUID.randomUUID();

        mvc.perform(post("/api/v1/notifications/inbox/{inboxId}/read", inboxId)
                        .with(asHr(hrEmpId)))
                .andExpect(status().isNoContent());

        verify(service).markReadSecured(eq(hrEmpId), eq(inboxId), eq(true), any(Instant.class));
    }

    @Test
    void deleteFromInbox_204_employee() throws Exception {
        UUID me = UUID.randomUUID();
        UUID inboxId = UUID.randomUUID();

        mvc.perform(delete("/api/v1/notifications/inbox/{inboxId}", inboxId)
                        .with(asEmployee(me)))
                .andExpect(status().isNoContent());

        verify(service).deleteFromInboxSecured(eq(me), eq(inboxId), eq(false), any(Instant.class));
    }
}
