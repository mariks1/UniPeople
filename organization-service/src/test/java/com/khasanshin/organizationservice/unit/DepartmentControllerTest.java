package com.khasanshin.organizationservice.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.organizationservice.controller.DepartmentController;
import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.service.DepartmentService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DepartmentController.class)
@Import(com.khasanshin.organizationservice.exception.GlobalExceptionHandler.class)
class DepartmentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean DepartmentService service;

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

    private static RequestPostProcessor asOrgAdmin() {
        return SecurityMockMvcRequestPostProcessors
                .jwt()
                .authorities(new SimpleGrantedAuthority("ORG_ADMIN"))
                .jwt(jwt -> jwt.claim("roles", List.of("ORG_ADMIN")));
    }


    @Test
    void list_200_withHeader() throws Exception {
        var dto = DepartmentDto.builder()
                .id(UUID.randomUUID())
                .name("Math")
                .build();
        var page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(service.findAll(any())).thenReturn(page);

        mvc.perform(get("/api/v1/departments").param("page","0").param("size","10").with(asHr())) // TODO
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$.content[0].id").value(dto.getId().toString()));
    }

    @Test
    void get_200() throws Exception {
        var id = UUID.randomUUID();
        var dto = DepartmentDto.builder().id(id).name("Physics").build();
        when(service.get(id)).thenReturn(dto);

        mvc.perform(get("/api/v1/departments/{id}", id).with(asEmployee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void get_404() throws Exception {
        when(service.get(any())).thenThrow(new EntityNotFoundException());
        mvc.perform(get("/api/v1/departments/{id}", UUID.randomUUID()).with(asEmployee()))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_201() throws Exception {
        UUID facultyId = UUID.randomUUID();
        var body = Map.of(
                "name", "Chemistry",
                "code", "CHEM",
                "faculty_id", facultyId.toString()
        );
        var saved = DepartmentDto.builder().id(UUID.randomUUID()).name("Chemistry").build();
        when(service.create(any(CreateDepartmentDto.class))).thenReturn(saved);

        mvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(body))
                        .with(asOrgAdmin()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()));
    }

    @Test
    void create_400_validation() throws Exception {
        var bad = Map.of("name", "");
        mvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(bad))
                        .with(asOrgAdmin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_200() throws Exception {
        var id = UUID.randomUUID();
        var req = Map.of("name", "NewName");
        var updated = DepartmentDto.builder().id(id).name("NewName").build();
        when(service.update(eq(id), any(UpdateDepartmentDto.class))).thenReturn(updated);

        mvc.perform(put("/api/v1/departments/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req))
                        .with(asOrgAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void delete_204() throws Exception {
        mvc.perform(delete("/api/v1/departments/{id}", UUID.randomUUID()).with(asOrgAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void setHead_200() throws Exception {
        var id = UUID.randomUUID();
        var emp = UUID.randomUUID();
        var dto = DepartmentDto.builder().id(id).headEmployeeId(emp).build();
        when(service.setHead(id, emp)).thenReturn(dto);

        mvc.perform(put("/api/v1/departments/{id}/head/{employeeId}", id, emp)
                        .with(asOrgAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void removeHead_204() throws Exception {
        mvc.perform(delete("/api/v1/departments/{id}/head", UUID.randomUUID()).with(asOrgAdmin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void head_exists_200_and_notExists_404() throws Exception {
        UUID existing = UUID.randomUUID();
        UUID missing  = UUID.randomUUID();

        when(service.exists(existing)).thenReturn(true);
        when(service.exists(missing)).thenReturn(false);

        mvc.perform(head("/api/v1/departments/{id}", existing).with(asEmployee()))
                .andExpect(status().isOk());

        mvc.perform(head("/api/v1/departments/{id}", missing).with(asEmployee()))
                .andExpect(status().isNotFound());
    }

    @Test
    void listEmployees_501_withHeader() throws Exception {
        mvc.perform(get("/api/v1/departments/{id}/employees", UUID.randomUUID())
                        .param("page","0").param("size","5")
                        .with(asOrgAdmin()))
                .andExpect(status().isNotImplemented())
                .andExpect(header().string("X-Total-Count", "0"));
    }
}
