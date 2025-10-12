package temp.unipeople.feature.department.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.department.dto.CreateDepartmentRequest;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.department.dto.UpdateDepartmentRequest;
import temp.unipeople.feature.department.service.DepartmentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
@Tag(name = "Department", description = "TODO") // TODO
public class DepartmentController {

  private final DepartmentService service;

  /** Пагинация + total в хедере */
  @GetMapping
  public ResponseEntity<Page<DepartmentDto>> findAll(Pageable pageable) {
    Page<DepartmentDto> page = service.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  /** Получить департамент по id */
  @GetMapping("/{id}")
  public ResponseEntity<DepartmentDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  /** Создать департамент */
  @PostMapping
  public ResponseEntity<DepartmentDto> create(@Valid @RequestBody CreateDepartmentRequest body) {
    var created = service.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Обновить департамент (имя, код, привязка к факультету) */
  @PutMapping("/{id}")
  public ResponseEntity<DepartmentDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateDepartmentRequest body) {
    return ResponseEntity.ok(service.update(id, body));
  }

  /** Удалить департамент */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /** Назначить/сменить заведующего кафедрой */
  @PutMapping("/{id}/head/{employeeId}")
  public ResponseEntity<DepartmentDto> setHead(
      @PathVariable UUID id, @PathVariable UUID employeeId) {
    DepartmentDto dto = service.setHead(id, employeeId);
    return ResponseEntity.ok(dto);
  }

  /** Снять заведующего кафедрой */
  @DeleteMapping("/{id}/head")
  public ResponseEntity<Void> removeHead(@PathVariable UUID id) {
    service.removeHead(id);
    return ResponseEntity.noContent().build();
  }

  /** Список сотрудников кафедры (текущие) — пагинация + total */
  @GetMapping("/{id}/employees")
  public ResponseEntity<Page<Object>> listEmployees(@PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

  // TODO
  // ===== Обязанности в рамках департамента =====
  // Для этого на уровне БД нужна таблица назначений обязанностей (M:N c payload), см. ниже
  // комментарий.

  /** Назначить обязанность сотруднику в департаменте */
  @PostMapping("/{id}/duties/assign")
  public ResponseEntity<Object> assignDuty(
      @PathVariable UUID id,
      @RequestBody Map<String, Object> body /* {employeeId, dutyId, note?} */) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Список назначений обязанностей в департаменте (пагинация) */
  @GetMapping("/{id}/duties/assignments")
  public ResponseEntity<Page<Object>> listDutyAssignments(
      @PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

  /** Отменить назначенную обязанность */
  @DeleteMapping("/{id}/duties/assignments/{assignmentId}")
  public ResponseEntity<Void> unassignDuty(@PathVariable UUID id, @PathVariable UUID assignmentId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
