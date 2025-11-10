package com.khasanshin.leaveservice;

import com.khasanshin.leaveservice.controller.LeaveRequestController;
import com.khasanshin.leaveservice.dto.CreateLeaveRequestDto;
import com.khasanshin.leaveservice.dto.DecisionDto;
import com.khasanshin.leaveservice.dto.LeaveRequestDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveRequestDto;
import com.khasanshin.leaveservice.entity.LeaveRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = LeaveRequestController.class)
@Import(GlobalExceptionHandler.class)
class LeaveRequestControllerTest {

    @Autowired
    WebTestClient web;

    @MockitoBean
    LeaveService service;

    @Test
    void get_200() {
        UUID id = UUID.randomUUID();
        var dto = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.PENDING).build();
        when(service.get(id)).thenReturn(Mono.just(dto));

        web.get().uri("/api/v1/leave-requests/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void get_404() {
        UUID id = UUID.randomUUID();
        when(service.get(id)).thenReturn(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "not found")));

        web.get().uri("/api/v1/leave-requests/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void create_200_ok() {
        UUID id = UUID.randomUUID();
        var saved = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.PENDING).build();
        when(service.create(any(CreateLeaveRequestDto.class))).thenReturn(Mono.just(saved));

        var body = Map.of(
                "employee_id", UUID.randomUUID().toString(),
                "type_id",     UUID.randomUUID().toString(),
                "date_from",   LocalDate.now().toString(),
                "date_to",     LocalDate.now().plusDays(5).toString(),
                "comment",     "Vacation"
        );

        web.post().uri("/api/v1/leave-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void create_400_validation() {
        web.post().uri("/api/v1/leave-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void update_200() {
        UUID id = UUID.randomUUID();
        var updated = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.PENDING).build();
        when(service.update(eq(id), any(UpdateLeaveRequestDto.class))).thenReturn(Mono.just(updated));

        var body = Map.of("comment", "fix dates");
        web.patch().uri("/api/v1/leave-requests/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void approve_200() {
        UUID id = UUID.randomUUID();
        var dto = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.APPROVED).build();
        when(service.approve(eq(id), any(DecisionDto.class))).thenReturn(Mono.just(dto));

        var body = Map.of(
                "approver_id", UUID.randomUUID().toString(),
                "comment", "ok"
        );

        web.post().uri("/api/v1/leave-requests/{id}/approve", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("APPROVED");
    }

    @Test
    void reject_200() {
        UUID id = UUID.randomUUID();
        var dto = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.REJECTED).build();
        when(service.reject(eq(id), any(DecisionDto.class))).thenReturn(Mono.just(dto));

        var body = Map.of(
                "approver_id", UUID.randomUUID().toString(),
                "comment", "nope"
        );

        web.post().uri("/api/v1/leave-requests/{id}/reject", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("REJECTED");
    }

    @Test
    void cancel_200() {
        UUID id = UUID.randomUUID();
        var dto = LeaveRequestDto.builder().id(id).status(LeaveRequest.Status.CANCELED).build();
        when(service.cancel(id)).thenReturn(Mono.just(dto));

        web.post().uri("/api/v1/leave-requests/{id}/cancel", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("CANCELED");
    }

    @Test
    void byEmployee_200_withHeaderAndBody() {
        UUID emp = UUID.randomUUID();
        var dto1 = LeaveRequestDto.builder().id(UUID.randomUUID()).status(LeaveRequest.Status.PENDING).build();
        var dto2 = LeaveRequestDto.builder().id(UUID.randomUUID()).status(LeaveRequest.Status.APPROVED).build();

        when(service.countLeaveByEmployee(emp)).thenReturn(Mono.just(42L));
        when(service.listByEmployee(eq(emp), any()))
                .thenReturn(Flux.fromIterable(List.of(dto1, dto2)));

        web.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/leave-requests/by-employee/{emp}")
                        .queryParam("page", 0)
                        .queryParam("size", 5)
                        .build(emp))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "42")
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(dto1.getId().toString())
                .jsonPath("$[1].id").isEqualTo(dto2.getId().toString());
    }

    @Test
    void byStatus_200_withHeaderAndBody() {
        var status = LeaveRequest.Status.PENDING;
        var dto = LeaveRequestDto.builder().id(UUID.randomUUID()).status(status).build();

        when(service.countLeaveByStatus(status)).thenReturn(Mono.just(7L));
        when(service.listByStatus(eq(status), any())).thenReturn(Flux.just(dto));

        web.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/leave-requests")
                        .queryParam("status", status)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "7")
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("PENDING");
    }
}
