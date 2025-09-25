package temp.unipeople.feature.employee.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

  /**
   * Пагинация с total*/
  @GetMapping
  public ResponseEntity<Page<Object>> findAll(Pageable pageable) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Бесконечная прокрутка (без total). Курсор — по createdAt (Instant).
   * TODO: вернуть Slice<EmployeeResponseDTO> и вычислять nextCursor как createdAt последнего элемента.
   */
  @GetMapping("/stream")
  public ResponseEntity<Map<String, Object>> stream(
          @RequestParam(required = false) Instant cursor,
          @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Просмотр сотрудника по id.
   */
  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Создание сотрудника.
   */
  @PostMapping
  public ResponseEntity<Object> create(@RequestBody Object body /* EmployeeCreateRequestDTO */) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Полное обновление (заменяет все изменяемые поля).
   */
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
          @PathVariable UUID id, @RequestBody Object body /* EmployeeUpdateRequestDTO */) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Частичное обновление (все поля опциональны).
   */
  @PatchMapping("/{id}")
  public ResponseEntity<Object> patch(
          @PathVariable UUID id, @RequestBody Map<String, Object> patch /* EmployeePatchRequestDTO */) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Удаление сотрудника (или пометить как FIRED — на ваш выбор в сервисе).
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Доменная операция: уволить (status = FIRED).
   */
  @PostMapping("/{id}/fire")
  public ResponseEntity<Object> fire(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /**
   * Доменная операция: активировать (status = ACTIVE).
   */
  @PostMapping("/{id}/activate")
  public ResponseEntity<Object> activate(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }
}
