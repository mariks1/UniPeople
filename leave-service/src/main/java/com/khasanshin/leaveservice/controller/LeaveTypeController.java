package com.khasanshin.leaveservice.controller;

import com.khasanshin.leaveservice.application.LeaveTypeUseCase;
import com.khasanshin.leaveservice.dto.CreateLeaveTypeDto;
import com.khasanshin.leaveservice.dto.LeaveTypeDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveTypeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
@Tag(name = "LeaveType", description = "Справочник видов отпусков")
public class LeaveTypeController {

  private final LeaveTypeUseCase service;

  @Operation(
      summary = "Список типов отпусков (пагинация)",
      description = "X-Total-Count в заголовке.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  public Flux<LeaveTypeDto> list(
          @ParameterObject
          @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable p,
          ServerHttpResponse response) {
    return service.countTypes()
            .doOnNext(total -> response.getHeaders().set("X-Total-Count", String.valueOf(total)))
            .thenMany(service.listTypes(p));
  }

  @Operation(summary = "Создать тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public Mono<LeaveTypeDto> create(@Valid @RequestBody CreateLeaveTypeDto body) {
    return service.createType(body);
  }

  @Operation(summary = "Обновить тип отпуска")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PatchMapping("/{id}")
  public Mono<LeaveTypeDto> update(
          @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveTypeDto body) {
    return service.updateType(id, body);
  }

  @Operation(summary = "Удалить тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404"),
    @ApiResponse(responseCode = "409", description = "Есть связанные заявки")
  })
  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable("id") UUID id) {
    return service.deleteType(id);
  }
}
