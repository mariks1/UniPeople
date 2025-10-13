package temp.unipeople.feature.duty.controller;

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
import temp.unipeople.feature.duty.dto.*;
import temp.unipeople.feature.duty.service.DutyAssignmentService;
import temp.unipeople.feature.duty.service.DutyService;

@RestController
@RequestMapping("/api/v1/duties")
@RequiredArgsConstructor
@Tag(name = "Duty", description = "Справочник обязанностей и назначения в департаментах")
public class DutyController {

  private final DutyService service;
  private final DutyAssignmentService assignmentService;

  @Operation(
      summary = "Список обязанностей (пагинация)",
      description = "X-Total-Count — общее количество.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее число записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  public ResponseEntity<Page<DutyDto>> findAll(Pageable pageable) {
    Page<DutyDto> page = service.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @Operation(summary = "Получить обязанность по ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Обязанность не найдена")
  })
  @GetMapping("/{id}")
  public ResponseEntity<DutyDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @Operation(summary = "Создать обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public ResponseEntity<DutyDto> create(@Valid @RequestBody CreateDutyDto dto) {
    DutyDto created = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Обязанность не найдена"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PutMapping("/{id}")
  public ResponseEntity<DutyDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateDutyDto dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  @Operation(summary = "Удалить обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404", description = "Обязанность не найдена"),
    @ApiResponse(responseCode = "409", description = "Есть связанные назначения")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Назначения по обязанности (пагинация)",
      description = "Для аналитики/поиска. X-Total-Count в заголовке.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/{id}/assignments")
  public ResponseEntity<Page<DutyAssignmentDto>> listAssignments(
      @PathVariable("id") UUID id, Pageable pageable) {
    Page<DutyAssignmentDto> page = service.listAssignments(id, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @Operation(
      summary = "Список назначений обязанностей в департаменте (пагинация)",
      description = "X-Total-Count — общее количество записей")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее число записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/departments/{id}/assignments")
  public ResponseEntity<Page<DutyAssignmentDto>> listDutyAssignmentsByDepartment(
      @PathVariable("id") UUID departmentId, Pageable pageable) {

    Page<DutyAssignmentDto> page = assignmentService.list(departmentId, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @Operation(
      summary = "Назначить обязанность сотруднику в департаменте",
      description = "Создаёт запись назначения duty → employee внутри департамента")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Валидaция/некорректные данные"),
    @ApiResponse(responseCode = "404", description = "Не найден department/employee/duty"),
    @ApiResponse(responseCode = "409", description = "Уже назначено (уникальный ключ)"),
  })
  @PostMapping("/departments/{departmentId}/assignments")
  public ResponseEntity<DutyAssignmentDto> assignDutyToEmployee(
      @PathVariable("departmentId") UUID departmentId, @Valid @RequestBody AssignDutyDto req) {

    DutyAssignmentDto created = assignmentService.assign(departmentId, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Снять назначение по ID для департамента")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(
        responseCode = "404",
        description = "Назначение не найдено или не в этом департаменте"),
  })
  @DeleteMapping("/departments/{departmentId}/assignments/{assignmentId}")
  public ResponseEntity<Void> unassignDuty(
      @PathVariable("departmentId") UUID departmentId,
      @PathVariable("assignmentId") UUID assignmentId) {

    assignmentService.unassign(departmentId, assignmentId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Список назначений обязанностей в департаменте (пагинация)",
      description = "X-Total-Count — общее количество записей")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее число записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/{id}/duties/assignments")
  public ResponseEntity<Page<DutyAssignmentDto>> listDutyAssignmentsLegacy(
      @PathVariable("id") UUID departmentId, Pageable pageable) {

    Page<DutyAssignmentDto> page = assignmentService.list(departmentId, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }
}
