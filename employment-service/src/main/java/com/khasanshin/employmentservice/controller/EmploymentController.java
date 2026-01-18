package com.khasanshin.employmentservice.controller;

import com.khasanshin.employmentservice.application.EmploymentUseCase;
import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
@Tag(
    name = "Employment",
    description = "Штатные назначения: создание, обновление, закрытие, выборки")
public class EmploymentController {

  private final EmploymentUseCase employmentService;

  @Operation(summary = "Получить назначение по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public Mono<EmploymentDto> get(@PathVariable("id") UUID id) {
      return employmentService.get(id);
  }

  @Operation(summary = "Создать назначение (приём/перевод)")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/ставка/перекрытия"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности/целостности")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public Mono<EmploymentDto> create(@Valid @RequestBody CreateEmploymentDto body) { // TODO
      return employmentService.create(body);
  }

  @Operation(summary = "Обновить назначение (ставка/оклад/дата окончания)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public Mono<EmploymentDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateEmploymentDto body) {
      return employmentService.update(id, body);
  }

  @Operation(summary = "Закрыть назначение (endDate, status=CLOSED)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/close")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public Mono<EmploymentDto> close(
      @PathVariable("id") UUID id, @Valid @RequestBody(required = false) CloseEmploymentDto body) {
      return employmentService.close(id, body);
  }

  @Operation(summary = "История назначений сотрудника (пагинация)")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/by-employee/{employeeId}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication,#employeeId)")
  public Flux<EmploymentDto> listByEmployee(
          @PathVariable("employeeId") UUID employeeId,
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "size", defaultValue = "20") int size,
          ServerHttpResponse response) {

      int p = Math.max(page, 0);
      int s = Math.min(Math.max(size, 1), 200);
      response.beforeCommit(() ->
              employmentService.countByEmployee(employeeId)
                      .doOnNext(total -> response.getHeaders().set("X-Total-Count", String.valueOf(total)))
                      .then()
      );

      return employmentService.listByEmployee(employeeId, p, s);
  }

  @Operation(summary = "Назначения по департаменту (active=true по умолчанию)")
  @Parameters(
      @Parameter(name = "active", description = "Только активные на сегодня (default=true)"))
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/by-department/{departmentId}")
  @PreAuthorize("@perm.canManageDept(authentication,#departmentId)")
  public Flux<EmploymentDto> listByDepartment(
          @PathVariable("departmentId") UUID departmentId,
          @RequestParam(name = "active", defaultValue = "true") boolean active,
          @RequestParam(name = "page", defaultValue = "0") int page,
          @RequestParam(name = "size", defaultValue = "20") int size,
          ServerHttpResponse response) {

      int p = Math.max(page, 0);
      int s = Math.min(Math.max(size, 1), 200);
      response.beforeCommit(() ->
              employmentService.countByDepartment(departmentId, active)
                      .doOnNext(total -> response.getHeaders().set("X-Total-Count", String.valueOf(total)))
                      .then()
      );

      return employmentService.listByDepartment(departmentId, active, p, s);
  }
}
