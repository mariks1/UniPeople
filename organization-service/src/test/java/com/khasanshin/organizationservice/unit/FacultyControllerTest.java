package com.khasanshin.organizationservice.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.organizationservice.controller.FacultyController;
import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import com.khasanshin.organizationservice.service.FacultyService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FacultyController.class)
@Import(com.khasanshin.organizationservice.exception.GlobalExceptionHandler.class)
class FacultyControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockitoBean FacultyService service;

    @Test
    void get_200() throws Exception {
        var id = UUID.randomUUID();
        var dto = FacultyDto.builder().id(id).name("IT").build();
        when(service.get(id)).thenReturn(dto);

        mvc.perform(get("/api/v1/faculties/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void get_404() throws Exception {
        when(service.get(any())).thenThrow(new EntityNotFoundException());
        mvc.perform(get("/api/v1/faculties/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_201() throws Exception {
        var body = Map.of("name", "Design", "code", "DES");
        var saved = FacultyDto.builder().id(UUID.randomUUID()).name("Design").build();
        when(service.create(any(CreateFacultyDto.class))).thenReturn(saved);

        mvc.perform(post("/api/v1/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(saved.getId().toString()));
    }

    @Test
    void create_400_validation() throws Exception {
        var bad = Map.of("name", ""); // нарушь свои @NotBlank
        mvc.perform(post("/api/v1/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_200() throws Exception {
        var id = UUID.randomUUID();
        var req = Map.of("name", "NewFaculty");
        var dto = FacultyDto.builder().id(id).name("NewFaculty").build();
        when(service.update(eq(id), any(UpdateFacultyDto.class))).thenReturn(dto);

        mvc.perform(put("/api/v1/faculties/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewFaculty"));
    }
}
