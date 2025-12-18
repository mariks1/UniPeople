package com.khasanshin.employeeservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.employeeservice.controller.EmployeeController;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.exception.GlobalExceptionHandler;
import com.khasanshin.employeeservice.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
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

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = EmployeeController.class)
@Import(GlobalExceptionHandler.class)
class EmployeeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockitoBean EmployeeService service;

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
    void get_ok() throws Exception {
        UUID id = UUID.randomUUID();
        var dto = EmployeeDto.builder().id(id).firstName("A").lastName("B").build();
        when(service.get(id)).thenReturn(dto);

        mvc.perform(get("/api/v1/employees/{id}", id).with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.first_name").value("A"));
    }

    @Test
    void get_404() throws Exception {
        when(service.get(any())).thenThrow(new EntityNotFoundException());

        mvc.perform(get("/api/v1/employees/{id}", UUID.randomUUID()).with(asHr()))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_201() throws Exception {
        var id = UUID.randomUUID();
        var created = EmployeeDto.builder().id(id).firstName("A").lastName("B").build();
        when(service.create(any())).thenReturn(created);

        var body = Map.of("first_name","A","last_name","B", "work_email", "a@b.c");

        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(body))
                        .with(asHr()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.first_name").value("A"));
    }

    @Test
    void create_400_validation() throws Exception {
        var bad = Map.of("first_name", "", "last_name", "");

        mvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(bad))
                        .with(asHr()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_415_whenNoContentType() throws Exception {
        var body = Map.of("first_name","A","last_name","B");

        mvc.perform(post("/api/v1/employees")
                        .content(mapper.writeValueAsBytes(body))
                        .with(asHr()))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void update_200() throws Exception {
        var id = UUID.randomUUID();
        var updated = EmployeeDto.builder().id(id).firstName("X").lastName("Y").build();
        when(service.update(eq(id), any())).thenReturn(updated);

        var body = Map.of("first_name","X");

        mvc.perform(put("/api/v1/employees/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(body))
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.first_name").value("X"));
    }

    @Test
    void update_404() throws Exception {
        when(service.update(any(), any())).thenThrow(new EntityNotFoundException());

        mvc.perform(put("/api/v1/employees/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(Map.of("first_name","X")))
                        .with(asHr()))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_204() throws Exception {
        doNothing().when(service).delete(any());

        mvc.perform(delete("/api/v1/employees/{id}", UUID.randomUUID()).with(asHr()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_404() throws Exception {
        var id = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new EntityNotFoundException()).when(service).delete(eq(id));

        mvc.perform(delete("/api/v1/employees/{id}", id).with(asHr()))
                .andExpect(status().isNotFound());
    }

    @Test
    void fire_200() throws Exception {
        var id = UUID.randomUUID();
        var dto = EmployeeDto.builder().id(id).firstName("A").lastName("B").build();
        when(service.fire(id)).thenReturn(dto);

        mvc.perform(post("/api/v1/employees/{id}/fire", id).with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void activate_200() throws Exception {
        var id = UUID.randomUUID();
        var dto = EmployeeDto.builder().id(id).firstName("A").lastName("B").build();
        when(service.activate(id)).thenReturn(dto);

        mvc.perform(post("/api/v1/employees/{id}/activate", id).with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void head_200_whenExists() throws Exception {
        var id = UUID.randomUUID();
        when(service.exists(id)).thenReturn(true);

        mvc.perform(head("/api/v1/employees/{id}", id).with(asHr()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void head_404_whenNotExists() throws Exception {
        var id = UUID.randomUUID();
        when(service.exists(id)).thenReturn(false);

        mvc.perform(head("/api/v1/employees/{id}", id).with(asHr()))
                .andExpect(status().isNotFound());
    }

    @Test
    void page_ok_withSortAndPageable_andHeader() throws Exception {
        var dto = EmployeeDto.builder().id(UUID.randomUUID()).firstName("A").lastName("B").build();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 42);
        when(service.findAll(any())).thenReturn(page);

        mvc.perform(get("/api/v1/employees").param("page","0").param("size","10").with(asHr()))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "42"))
                .andExpect(jsonPath("$.content[0].first_name").value("A"))
                .andExpect(jsonPath("$.totalElements").value(42));
    }

    @Test
    void stream_ok() throws Exception {
        var id = UUID.randomUUID();
        var dto = EmployeeDto.builder()
                .id(id).firstName("A").lastName("B")
                .createdAt(Instant.parse("2024-01-01T00:00:00Z"))
                .build();

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("items", List.of(dto));
        payload.put("hasNext", true);
        payload.put("nextCursor", "2024-01-01T00:00:00Z");

        when(service.stream(any(), any(Integer.class))).thenReturn(payload);

        mvc.perform(get("/api/v1/employees/stream")
                        .param("size","20")
                        .param("cursor","2023-12-31T00:00:00Z")
                        .with(asHr()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id.toString()))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").value("2024-01-01T00:00:00Z"));
    }
}
