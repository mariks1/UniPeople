package com.khasanshin.employmentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.khasanshin.employmentservice.controller.EmploymentController;
import com.khasanshin.employmentservice.dto.*;
import com.khasanshin.employmentservice.service.EmploymentService;
import com.khasanshin.employmentservice.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@WebFluxTest(controllers = EmploymentController.class)
@Import(GlobalExceptionHandler.class)
class EmploymentControllerTest {

    @Autowired WebTestClient webTestClient;
    @Autowired ObjectMapper mapper;

    @MockitoBean EmploymentService service;

    @Test
    void get_200() {
        UUID id = UUID.randomUUID();
        EmploymentDto dto = EmploymentDto.builder().id(id).build();

        when(service.get(id)).thenReturn(Mono.just(dto));

        webTestClient.get().uri("/api/v1/employments/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void get_404() {
        when(service.get(any())).thenReturn(Mono.error(new jakarta.persistence.EntityNotFoundException()));

        webTestClient.get().uri("/api/v1/employments/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void create_201() {
        var body = Map.of(
                "employee_id", UUID.randomUUID().toString(),
                "department_id", UUID.randomUUID().toString(),
                "position_id", UUID.randomUUID().toString(),
                "start_date", "2024-01-01"
        );
        EmploymentDto created = EmploymentDto.builder().id(UUID.randomUUID()).build();

        when(service.create(any(CreateEmploymentDto.class))).thenReturn(Mono.just(created));

        webTestClient.post().uri("/api/v1/employments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(created.getId().toString());
    }

    @Test
    void create_400_validation() {
        var bad = Map.of();

        webTestClient.post().uri("/api/v1/employments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bad)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void create_415_whenWrongContentType() {
        var anyBody = "{\"x\":1}";

        webTestClient.post().uri("/api/v1/employments")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(anyBody)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void update_200() {
        UUID id = UUID.randomUUID();
        var req = Map.of("rate", 0.5);
        EmploymentDto updated = EmploymentDto.builder().id(id).build();

        when(service.update(eq(id), any(UpdateEmploymentDto.class))).thenReturn(Mono.just(updated));

        webTestClient.put().uri("/api/v1/employments/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void close_200() {
        UUID id = UUID.randomUUID();
        var req = Map.of("end_date", "2025-12-31");
        EmploymentDto result = EmploymentDto.builder().id(id).build();

        when(service.close(eq(id), any(CloseEmploymentDto.class))).thenReturn(Mono.just(result));

        webTestClient.post().uri("/api/v1/employments/{id}/close", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }


    @Test
    void listByEmployee_200_withHeader_andFluxBody() {
        var dto = EmploymentDto.builder().id(UUID.randomUUID()).build();

        when(service.listByEmployee(any(), anyInt(), anyInt()))
                .thenReturn(reactor.core.publisher.Flux.just(dto));
        when(service.countByEmployee(any()))
                .thenReturn(reactor.core.publisher.Mono.just(1L));

        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employments/by-employee/{id}")
                        .queryParam("page","0").queryParam("size","10")
                        .build(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(dto.getId().toString());
    }

    @Test
    void listByDepartment_200_withHeader_defaultActiveTrue_andFluxBody() {
        var dto = EmploymentDto.builder().id(UUID.randomUUID()).build();

        when(service.listByDepartment(any(), eq(true), anyInt(), anyInt()))
                .thenReturn(reactor.core.publisher.Flux.just(dto));
        when(service.countByDepartment(any(), eq(true)))
                .thenReturn(reactor.core.publisher.Mono.just(1L));

        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employments/by-department/{depId}")
                        .queryParam("page","0").queryParam("size","5")
                        .build(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(dto.getId().toString());
    }

    @Test
    void listByDepartment_200_withHeader_activeFalse_andFluxBody() {
        var dto = EmploymentDto.builder().id(UUID.randomUUID()).build();

        when(service.listByDepartment(any(), eq(false), anyInt(), anyInt()))
                .thenReturn(reactor.core.publisher.Flux.just(dto));
        when(service.countByDepartment(any(), eq(false)))
                .thenReturn(reactor.core.publisher.Mono.just(1L));

        webTestClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/employments/by-department/{depId}")
                        .queryParam("active","false")
                        .queryParam("page","0").queryParam("size","5")
                        .build(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "1")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(dto.getId().toString());
    }

}
