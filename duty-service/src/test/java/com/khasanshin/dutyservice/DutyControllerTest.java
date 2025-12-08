package com.khasanshin.dutyservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.dutyservice.controller.DutyController;
import com.khasanshin.dutyservice.dto.CreateDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.dto.DutyDto;
import com.khasanshin.dutyservice.dto.UpdateDutyDto;
import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.service.DutyAssignmentService;
import com.khasanshin.dutyservice.service.DutyService;
import com.khasanshin.dutyservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = DutyController.class)
@Import(GlobalExceptionHandler.class)
class DutyControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean
    DutyService dutyService;
    @MockitoBean
    DutyAssignmentService assignmentService;
    @MockitoBean
    JwtDecoder jwtDecoder;

    private static RequestPostProcessor asHr() {
        return SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_HR"))
                .jwt(jwt -> jwt.claim("roles", List.of("HR")));
    }

    private static RequestPostProcessor asEmployee() {
        return SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority("EMPLOYEE"))
                .jwt(jwt -> jwt.claim("roles", List.of("EMPLOYEE")));
    }

    @Test
    void findAll_200_withHeader() throws Exception {
        var dto = DutyDto.builder()
                .id(UUID.randomUUID())
                .code("D-001")
                .name("Safety")
                .build();

        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        when(dutyService.findAll(any())).thenReturn(page);

        mvc.perform(get("/api/v1/duty").param("page","0").param("size","10").with(asEmployee()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(dto.getId().toString()));
    }

    @Test
    void get_200() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = DutyDto.builder().id(id).code("D-001").name("Safety").build();
        when(dutyService.get(id)).thenReturn(dto);

        mvc.perform(get("/api/v1/duty/{id}", id).with(asEmployee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Safety"));
    }

    @Test
    void get_404() throws Exception {
        when(dutyService.get(any())).thenThrow(new jakarta.persistence.EntityNotFoundException());
        mvc.perform(get("/api/v1/duty/{id}", UUID.randomUUID()).with(asEmployee()))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_201() throws Exception {
        var req = Map.of(
                "code", "D-002",
                "name", "Fire safety"
        );

        var created = DutyDto.builder()
                .id(UUID.randomUUID())
                .code("D-002").name("Fire safety").build();

        when(dutyService.create(any(CreateDutyDto.class))).thenReturn(created);

        mvc.perform(post("/api/v1/duty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req))
                        .with(asHr()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.code").value("D-002"));
    }

    @Test
    void create_400_validation() throws Exception {
        var bad = Map.of("code","", "name","");

        mvc.perform(post("/api/v1/duty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(bad))
                        .with(asHr()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_415_whenNoContentType() throws Exception {
        var anyBody = Map.of("code","D-003","name","X");
        mvc.perform(post("/api/v1/duty")
                        .content(mapper.writeValueAsBytes(anyBody))
                        .with(asHr()))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void update_200() throws Exception {
        UUID id = UUID.randomUUID();
        var req = Map.of("name", "Updated name");

        var updated = DutyDto.builder().id(id).code("D-001").name("Updated name").build();
        when(dutyService.update(eq(id), any(UpdateDutyDto.class))).thenReturn(updated);

        mvc.perform(put("/api/v1/duty/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req))
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated name"));
    }

    @Test
    void delete_204() throws Exception {
        mvc.perform(delete("/api/v1/duty/{id}", UUID.randomUUID()).with(asHr()))
                .andExpect(status().isNoContent());
    }

    @Test
    void listAssignments_200_withHeader() throws Exception {
        var a = DutyAssignmentDto.builder()
                .id(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .dutyId(UUID.randomUUID())
                .build();

        var page = new PageImpl<>(List.of(a), PageRequest.of(0, 20), 1);
        when(dutyService.listAssignments(any(), any())).thenReturn(page);

        mvc.perform(get("/api/v1/duty/{id}/assignments", UUID.randomUUID())
                        .param("page", "0").param("size", "20")
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(a.getId().toString()));
    }

    @Test
    void listAssignmentsByDepartment_200_withHeader() throws Exception {
        var a = DutyAssignmentDto.builder()
                .id(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .dutyId(UUID.randomUUID())
                .build();

        var page = new PageImpl<>(List.of(a), PageRequest.of(0, 10), 1);
        when(assignmentService.list(any(), any())).thenReturn(page);

        mvc.perform(get("/api/v1/duty/departments/{id}/assignments", UUID.randomUUID())
                        .param("page","0").param("size","10")
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(a.getId().toString()));
    }

    @Test
    void assignDuty_201() throws Exception {
        UUID deptId = UUID.randomUUID();

        var req = Map.of(
                "employee_id", UUID.randomUUID().toString(),
                "duty_id", UUID.randomUUID().toString()
        );

        var created = DutyAssignmentDto.builder()
                .id(UUID.randomUUID())
                .departmentId(deptId)
                .employeeId(UUID.fromString(req.get("employee_id")))
                .dutyId(UUID.fromString(req.get("duty_id")))
                .build();

        when(assignmentService.assign(eq(deptId), any(AssignDutyDto.class))).thenReturn(created);

        mvc.perform(post("/api/v1/duty/departments/{departmentId}/assignments", deptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req))
                        .with(asHr()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(created.getId().toString()));
    }

    @Test
    void unassign_204() throws Exception {
        mvc.perform(delete("/api/v1/duty/departments/{departmentId}/assignments/{assignmentId}",
                        UUID.randomUUID(), UUID.randomUUID()).with(asHr()))
                .andExpect(status().isNoContent());
    }

    @Test
    void listAssignmentsLegacy_200_withHeader() throws Exception {
        var a = DutyAssignmentDto.builder()
                .id(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .employeeId(UUID.randomUUID())
                .dutyId(UUID.randomUUID())
                .build();

        var page = new PageImpl<>(List.of(a), PageRequest.of(0, 5), 1);
        when(assignmentService.list(any(), any())).thenReturn(page);

        mvc.perform(get("/api/v1/duty/{id}/duties/assignments", UUID.randomUUID())
                        .param("page","0").param("size","5")
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(a.getId().toString()));
    }
}
