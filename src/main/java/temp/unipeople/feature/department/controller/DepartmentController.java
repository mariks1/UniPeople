package temp.unipeople.feature.department.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

  // TODO: сервис заинжектить

  /** Пагинация + total в хедере */
  @GetMapping
  public ResponseEntity<Page<Object>> findAll(Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0"); // TODO: real total
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

  /** Получить департамент по id */
  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Создать департамент */
  @PostMapping
  public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Обновить департамент (имя, код, привязка к факультету) */
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable UUID id, @RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Удалить департамент */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Назначить/сменить заведующего кафедрой */
  @PutMapping("/{id}/head/{employeeId}")
  public ResponseEntity<Void> setHead(@PathVariable UUID id, @PathVariable UUID employeeId) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Снять заведующего кафедрой */
  @DeleteMapping("/{id}/head")
  public ResponseEntity<Void> removeHead(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Список сотрудников кафедры (текущие) — пагинация + total */
  @GetMapping("/{id}/employees")
  public ResponseEntity<Page<Object>> listEmployees(@PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

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
