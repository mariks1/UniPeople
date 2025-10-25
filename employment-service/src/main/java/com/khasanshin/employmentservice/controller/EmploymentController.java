package com.khasanshin.employmentservice.controller;

import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import com.khasanshin.employmentservice.service.EmploymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
@Tag(
    name = "Employment",
    description = "Штатные назначения: создание, обновление, закрытие, выборки")
public class EmploymentController {

  private final EmploymentService employmentService;

  @Operation(summary = "Получить назначение по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  public Mono<ResponseEntity<EmploymentDto>> get(@PathVariable("id") UUID id) {
    return employmentService.get(id).map(ResponseEntity::ok);
  }

  @Operation(summary = "Создать назначение (приём/перевод)")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/ставка/перекрытия"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности/целостности")
  })
  @PostMapping
  public Mono<ResponseEntity<EmploymentDto>> create(@Valid @RequestBody CreateEmploymentDto body) {
    return employmentService.create(body)
            .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
  }

  @Operation(summary = "Обновить назначение (ставка/оклад/дата окончания)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  public Mono<ResponseEntity<EmploymentDto>> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateEmploymentDto body) {
    return employmentService.update(id, body).map(ResponseEntity::ok);
  }

  @Operation(summary = "Закрыть назначение (endDate, status=CLOSED)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/close")
  public Mono<ResponseEntity<EmploymentDto>> close(
      @PathVariable("id") UUID id, @Valid @RequestBody(required = false) CloseEmploymentDto body) {
    return employmentService.close(id, body).map(ResponseEntity::ok);
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
  public Mono<ResponseEntity<Page<EmploymentDto>>> listByEmployee(
      @PathVariable("employeeId") UUID employeeId, Pageable pageable) {
    return employmentService
            .listByEmployee(employeeId, pageable)
            .map(page -> {
              HttpHeaders headers = new HttpHeaders();
              headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
              return new ResponseEntity<>(page, headers, HttpStatus.OK);
            });
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
  public Mono<ResponseEntity<Page<EmploymentDto>>> listByDepartment(
      @PathVariable("departmentId") UUID departmentId,
      @RequestParam(required = false, defaultValue = "true", value = "active") boolean active,
      Pageable pageable) {
    return employmentService
            .listByDepartment(departmentId, active, pageable)
            .map(page -> {
              HttpHeaders headers = new HttpHeaders();
              headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
              return new ResponseEntity<>(page, headers, HttpStatus.OK);
            });
  }
}
