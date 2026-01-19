package com.khasanshin.dutyservice.controller;

import com.khasanshin.dutyservice.application.DutyAssignmentUseCase;
import com.khasanshin.dutyservice.application.DutyUseCase;
import com.khasanshin.dutyservice.dto.*;
import com.khasanshin.dutyservice.event.DutyEventPublisher;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/duty")
@RequiredArgsConstructor
@Tag(name = "Duty", description = "Справочник обязанностей и назначения в департаментах")
public class DutyController {

  private final DutyUseCase service;
  private final DutyAssignmentUseCase assignmentService;
  private final DutyEventPublisher publisher;

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
  @PreAuthorize("isAuthenticated()")
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
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DutyDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @Operation(summary = "Создать обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<DutyDto> create(@Valid @RequestBody CreateDutyDto dto) {
    DutyDto created = service.create(dto);
    publisher.dutyCreated(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Обязанность не найдена"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PutMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<DutyDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateDutyDto dto) {
    DutyDto updated = service.update(id, dto);
    publisher.dutyUpdated(updated);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Удалить обязанность")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404", description = "Обязанность не найдена"),
    @ApiResponse(responseCode = "409", description = "Есть связанные назначения")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(id);
    publisher.dutyDeleted(id);
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
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN','HR')")
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
  @PreAuthorize("@perm.canManageDept(authentication,#departmentId)")
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
  @PreAuthorize("@perm.canManageDept(authentication,#departmentId)")
  public ResponseEntity<DutyAssignmentDto> assignDutyToEmployee(
          @PathVariable("departmentId") @P("departmentId") UUID departmentId, @Valid @RequestBody AssignDutyDto req) {

    DutyAssignmentDto created = assignmentService.assign(departmentId, req);
    publisher.dutyAssigned(created);
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
  @PreAuthorize("@perm.canManageDept(authentication,#departmentId)")
  public ResponseEntity<Void> unassignDuty(
      @PathVariable("departmentId") UUID departmentId,
      @PathVariable("assignmentId") UUID assignmentId) {

    DutyAssignmentDto removed = assignmentService.unassignAndReturn(departmentId, assignmentId);

    publisher.dutyUnassigned(
            assignmentId,
            removed.getEmployeeId(),
            removed.getDepartmentId(),
            removed.getDutyId()
    );

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
  @PreAuthorize("@perm.canManageDept(authentication,#departmentId)")
  public ResponseEntity<Page<DutyAssignmentDto>> listDutyAssignmentsLegacy(
      @PathVariable("id") UUID departmentId, Pageable pageable) {

    Page<DutyAssignmentDto> page = assignmentService.list(departmentId, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }
}
