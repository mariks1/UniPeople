package com.khasanshin.leaveservice;

import com.khasanshin.leaveservice.controller.LeaveTypeController;
import com.khasanshin.leaveservice.dto.CreateLeaveTypeDto;
import com.khasanshin.leaveservice.dto.LeaveTypeDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveTypeDto;
import com.khasanshin.leaveservice.exception.GlobalExceptionHandler;
import com.khasanshin.leaveservice.service.LeaveService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = LeaveTypeController.class)
@Import(GlobalExceptionHandler.class) // если есть
class LeaveTypeControllerTest {

    @Autowired
    WebTestClient web;

    @MockitoBean
    LeaveService service;

    @Test
    void list_200_withHeaderAndBody() {
        var t1 = LeaveTypeDto.builder().id(UUID.randomUUID()).code("VAC").name("Vacation").build();
        var t2 = LeaveTypeDto.builder().id(UUID.randomUUID()).code("SICK").name("Sick").build();

        when(service.countTypes()).thenReturn(Mono.just(100L));
        when(service.listTypes(any())).thenReturn(Flux.fromIterable(List.of(t1, t2)));

        web.get().uri(uri -> uri.path("/api/v1/leave-types")
                        .queryParam("page", 0)
                        .queryParam("size", 20)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "100")
                .expectBody()
                .jsonPath("$[0].code").isEqualTo("VAC")
                .jsonPath("$[1].code").isEqualTo("SICK");
    }

    @Test
    void create_200_ok() {
        var body = Map.of("code", "VAC", "name", "Vacation", "days_per_year", 28);
        var saved = LeaveTypeDto.builder().id(UUID.randomUUID()).code("VAC").name("Vacation").build();

        when(service.createType(any(CreateLeaveTypeDto.class))).thenReturn(Mono.just(saved));

        web.post().uri("/api/v1/leave-types")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(saved.getId().toString());
    }

    @Test
    void update_200() {
        UUID id = UUID.randomUUID();
        var body = Map.of("name", "Paid vacation", "days_per_year", 30);
        var updated = LeaveTypeDto.builder().id(id).code("VAC").name("Paid vacation").build();

        when(service.updateType(eq(id), any(UpdateLeaveTypeDto.class))).thenReturn(Mono.just(updated));

        web.patch().uri("/api/v1/leave-types/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Paid vacation");
    }

    @Test
    void update_404() {
        UUID id = UUID.randomUUID();
        when(service.updateType(eq(id), any(UpdateLeaveTypeDto.class)))
                .thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "…")));

        web.patch().uri("/api/v1/leave-types/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "X"))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void delete_200_now() {
        UUID id = UUID.randomUUID();
        when(service.deleteType(id)).thenReturn(Mono.empty());

        web.delete().uri("/api/v1/leave-types/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}
