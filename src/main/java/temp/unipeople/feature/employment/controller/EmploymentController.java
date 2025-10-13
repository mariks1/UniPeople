package temp.unipeople.feature.employment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.employment.dto.CloseEmploymentDto;
import temp.unipeople.feature.employment.dto.CreateEmploymentDto;
import temp.unipeople.feature.employment.dto.EmploymentDto;
import temp.unipeople.feature.employment.dto.UpdateEmploymentDto;
import temp.unipeople.feature.employment.service.EmploymentService;

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
  public ResponseEntity<EmploymentDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(employmentService.get(id));
  }

  @Operation(summary = "Создать назначение (приём/перевод)")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/ставка/перекрытия"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности/целостности")
  })
  @PostMapping
  public ResponseEntity<EmploymentDto> create(@RequestBody CreateEmploymentDto body) {
    EmploymentDto dto = employmentService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @Operation(summary = "Обновить назначение (ставка/оклад/дата окончания)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  public ResponseEntity<EmploymentDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateEmploymentDto body) {
    return ResponseEntity.ok(employmentService.update(id, body));
  }

  @Operation(summary = "Закрыть назначение (endDate, status=CLOSED)")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PostMapping("/{id}/close")
  public ResponseEntity<EmploymentDto> close(
      @PathVariable("id") UUID id, @RequestBody(required = false) CloseEmploymentDto body) {
    return ResponseEntity.ok(employmentService.close(id, body));
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
  public ResponseEntity<Page<EmploymentDto>> listByEmployee(
      @PathVariable("employeeId") UUID employeeId, Pageable pageable) {
    Page<EmploymentDto> page = employmentService.listByEmployee(employeeId, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
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
  public ResponseEntity<Page<EmploymentDto>> listByDepartment(
      @PathVariable("departmentId") UUID departmentId,
      @RequestParam(required = false, defaultValue = "true", value = "active") boolean active,
      Pageable pageable) {
    Page<EmploymentDto> page = employmentService.listByDepartment(departmentId, active, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }
}
