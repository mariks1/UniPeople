package com.khasanshin.employeeservice.controller;

import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import com.khasanshin.employeeservice.event.EmployeeEventPublisher;
import com.khasanshin.employeeservice.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee", description = "Сотрудники: CRUD + бесконечная прокрутка")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;
  private final EmployeeEventPublisher publisher;

  @Operation(
      summary = "Список сотрудников (пагинация)",
      description = "X-Total-Count содержит общее число записей.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее число записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<Page<EmployeeDto>> findAll(Pageable pageable) {
    Page<EmployeeDto> employees = employeeService.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(employees.getTotalElements()));
    return new ResponseEntity<>(employees, headers, HttpStatus.OK);
  }

  @Operation(
      summary = "Поток сотрудников (infinite scroll)",
      description =
          """
        Возвращает { items, hasNext, nextCursor }.
        Курсор — Instant (createdAt последнего элемента предыдущей страницы).
      """)
  @Parameters({
    @Parameter(
        name = "cursor",
        description = "Курсор (Instant, ISO-8601)",
        schema = @Schema(type = "string", format = "date-time")),
    @Parameter(
        name = "size",
        description = "Размер страницы, по умолчанию 20",
        schema = @Schema(type = "integer", maximum = "50"))
  })
  @GetMapping("/stream")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<Map<String, Object>> stream(
      @RequestParam(name = "cursor", required = false) Instant cursor,
      @RequestParam(name = "size", defaultValue = "20") int size) {
    Map<String, Object> body = employeeService.stream(cursor, size);
    return ResponseEntity.ok(body);
  }

  @Operation(summary = "Получить сотрудника по id")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Не найден")
  })
  @GetMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR') || @perm.isSelf(authentication,#id)")
  public ResponseEntity<EmployeeDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(employeeService.get(id));
  }

  @Operation(summary = "Создать сотрудника")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<EmployeeDto> create(@Valid @RequestBody CreateEmployeeDto body) {
    var created = employeeService.create(body);
    publisher.publishEmployeeEvent("EMPLOYEE_CREATED", created.getId(), created);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить сотрудника")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<EmployeeDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateEmployeeDto body) {
    var updated = employeeService.update(id, body);
    publisher.publishEmployeeEvent("EMPLOYEE_CREATED", updated.getId(), updated);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Удалить сотрудника")
  @ApiResponses({@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404")})
  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Уволить сотрудника (status=FIRED)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/fire")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<EmployeeDto> fire(@PathVariable("id") UUID id) {
    var fired = employeeService.fire(id);
    publisher.publishEmployeeEvent("EMPLOYEE_CREATED", fired.getId(), fired);
    return ResponseEntity.ok(fired);
  }

  @Operation(summary = "Активировать сотрудника (status=ACTIVE)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/activate")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<Object> activate(@PathVariable("id") UUID id) {
    var activated = employeeService.activate(id);
    publisher.publishEmployeeEvent("EMPLOYEE_CREATED", activated.getId(), activated);
    return ResponseEntity.ok(activated);
  }

  @Operation(
          summary = "Проверить существование сотрудника",
          description = "HEAD-запрос без тела. Возвращает 200, если сотрудник существует, иначе 404."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Сотрудник существует"),
          @ApiResponse(responseCode = "404", description = "Сотрудник не найден")
  })
  @PreAuthorize("isAuthenticated()")
  @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
  public ResponseEntity<Void> head(@PathVariable("id") UUID id) {
    return employeeService.exists(id)
            ? ResponseEntity.ok().build()
            : ResponseEntity.notFound().build();
  }

}
