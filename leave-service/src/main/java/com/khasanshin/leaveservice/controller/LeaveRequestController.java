package com.khasanshin.leaveservice.controller;

import com.khasanshin.leaveservice.dto.CreateLeaveRequestDto;
import com.khasanshin.leaveservice.dto.DecisionDto;
import com.khasanshin.leaveservice.dto.LeaveRequestDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveRequestDto;
import com.khasanshin.leaveservice.entity.LeaveRequest;
import com.khasanshin.leaveservice.event.LeaveEventPublisher;
import com.khasanshin.leaveservice.service.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "LeaveRequest", description = "Заявки на отпуск: создание, правка, согласование")
public class LeaveRequestController {

  private final LeaveService service;
  private final LeaveEventPublisher publisher;

  @Operation(summary = "Получить заявку на отпуск по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  public Mono<LeaveRequestDto> get(@PathVariable("id") UUID id) {
    return service.get(id);
  }

  @Operation(summary = "Создать заявку на отпуск")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/пересечения/лимит"),
    @ApiResponse(responseCode = "404", description = "Тип не найден")
  })
  @PostMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication, #body.employeeId)")
  public Mono<LeaveRequestDto> create(@P("body") @Valid @RequestBody CreateLeaveRequestDto body) {
      return service.create(body)
              .doOnSuccess(saved -> publisher.leaveCreated(saved.getId(), saved));
    }

  @Operation(summary = "Обновить заявку (DRAFT/PENDING)")
  @ApiResponses({
    @ApiResponse( responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PatchMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication, T(java.util.UUID).fromString(#id.toString()))")
  public Mono<LeaveRequestDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveRequestDto body) {
    return service.update(id, body);
  }

  @Operation(summary = "Одобрить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/approve")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR','DEPT_HEAD')")
  public Mono<LeaveRequestDto> approve(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return service.approve(id, body)
            .doOnSuccess(saved -> publisher.leaveApproved(saved.getId(), saved));
  }

  @Operation(summary = "Отклонить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/reject")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR','DEPT_HEAD')")
  public Mono<LeaveRequestDto> reject(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return service.reject(id, body)
            .doOnSuccess(saved -> publisher.leaveRejected(saved.getId(), saved));
  }

  @Operation(summary = "Отменить заявку (PENDING/APPROVED)")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/cancel")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication, T(java.util.UUID).fromString(#id.toString()))")
  public Mono<LeaveRequestDto> cancel(@PathVariable("id") UUID id) {
    return service.cancel(id)
            .doOnSuccess(saved -> publisher.leaveCanceled(saved.getId(), saved));
  }

  @Operation(summary = "Заявки сотрудника (пагинация)")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/by-employee/{employeeId}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication,#employeeId)")
  public Flux<LeaveRequestDto> byEmployee(
          @PathVariable("employeeId") UUID employeeId, Pageable p, ServerHttpResponse response) {
    return service.countLeaveByEmployee(employeeId)
            .doOnNext(total ->
                    response.getHeaders().add("X-Total-Count", String.valueOf(total)))
            .thenMany(service.listByEmployee(employeeId, p));
  }

  @Operation(summary = "Заявки по статусу (пагинация)")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public Flux<LeaveRequestDto> byStatus(
          @RequestParam("status") LeaveRequest.Status status, Pageable p, ServerHttpResponse response) {
    return service.countLeaveByStatus(status)
            .doOnNext(total ->
                    response.getHeaders().add("X-Total-Count", String.valueOf(total)))
            .thenMany(service.listByStatus(status, p));
  }
}
