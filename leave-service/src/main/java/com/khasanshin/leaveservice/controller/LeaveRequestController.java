package com.khasanshin.leaveservice.controller;

import com.khasanshin.leaveservice.dto.CreateLeaveRequestDto;
import com.khasanshin.leaveservice.dto.DecisionDto;
import com.khasanshin.leaveservice.dto.LeaveRequestDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveRequestDto;
import com.khasanshin.leaveservice.entity.LeaveRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
@Tag(name = "LeaveRequest", description = "Заявки на отпуск: создание, правка, согласование")
public class LeaveRequestController {

  private final LeaveService service;

  @Operation(summary = "Получить заявку на отпуск по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  public Mono<ResponseEntity<LeaveRequestDto>> get(@PathVariable("id") UUID id) {
    return service.get(id).map(ResponseEntity::ok);
  }

  @Operation(summary = "Создать заявку на отпуск")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/пересечения/лимит"),
    @ApiResponse(responseCode = "404", description = "Тип не найден")
  })
  @PostMapping
  public Mono<ResponseEntity<LeaveRequestDto>> create(@Valid @RequestBody CreateLeaveRequestDto body) {
    return service.create(body).map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
  }

  @Operation(summary = "Обновить заявку (DRAFT/PENDING)")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PatchMapping("/{id}")
  public Mono<ResponseEntity<LeaveRequestDto>> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveRequestDto body) {
    return service.update(id, body).map(ResponseEntity::ok);
  }

  @Operation(summary = "Одобрить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/approve")
  public Mono<ResponseEntity<LeaveRequestDto>> approve(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return service.approve(id, body).map(ResponseEntity::ok);
  }

  @Operation(summary = "Отклонить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/reject")
  public Mono<ResponseEntity<LeaveRequestDto>> reject(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return service.reject(id, body).map(ResponseEntity::ok);
  }

  @Operation(summary = "Отменить заявку (PENDING/APPROVED)")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/cancel")
  public Mono<ResponseEntity<LeaveRequestDto>> cancel(@PathVariable("id") UUID id) {
    return service.cancel(id).map(ResponseEntity::ok);
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
  public Mono<ResponseEntity<Page<LeaveRequestDto>>> byEmployee(
      @PathVariable("employeeId") UUID employeeId, Pageable p) {
    return service.listByEmployee(employeeId, p)
            .map(page -> {
              var h = new HttpHeaders();
              h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
              return new ResponseEntity<>(page, h, HttpStatus.OK);
            });
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
  public Mono<ResponseEntity<Page<LeaveRequestDto>>> byStatus(
          @RequestParam("status") LeaveRequest.Status status, Pageable p) {
    return service.listByStatus(status, p)
            .map(page -> {
              var h = new HttpHeaders();
              h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
              return new ResponseEntity<>(page, h, HttpStatus.OK);
            });
  }
}
