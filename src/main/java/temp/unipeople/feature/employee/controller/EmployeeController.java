package temp.unipeople.feature.employee.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.employee.dto.CreateEmployeeDto;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.employee.dto.UpdateEmployeeDto;
import temp.unipeople.feature.employee.service.EmployeeService;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee", description = "Сотрудники: CRUD + бесконечная прокрутка")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

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
  public ResponseEntity<Map<String, Object>> stream(
      @RequestParam(required = false) Instant cursor, @RequestParam(defaultValue = "20") int size) {
    Map<String, Object> body = employeeService.stream(cursor, size);
    return ResponseEntity.ok(body);
  }

  @Operation(summary = "Получить сотрудника по id")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Не найден")
  })
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(employeeService.get(id));
  }

  @Operation(summary = "Создать сотрудника")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public ResponseEntity<EmployeeDto> create(@RequestBody CreateEmployeeDto body) {
    var created = employeeService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить сотрудника")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  public ResponseEntity<EmployeeDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateEmployeeDto body) {
    return ResponseEntity.ok(employeeService.update(id, body));
  }

  @Operation(summary = "Удалить сотрудника")
  @ApiResponses({@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404")})
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Уволить сотрудника (status=FIRED)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/fire")
  public ResponseEntity<EmployeeDto> fire(@PathVariable UUID id) {
    return ResponseEntity.ok(employeeService.fire(id));
  }

  @Operation(summary = "Активировать сотрудника (status=ACTIVE)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/activate")
  public ResponseEntity<Object> activate(@PathVariable UUID id) {
    return ResponseEntity.ok(employeeService.activate(id));
  }
}
