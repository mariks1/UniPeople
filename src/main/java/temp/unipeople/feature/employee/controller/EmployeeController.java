package temp.unipeople.feature.employee.controller;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.employee.dto.CreateEmployeeRequest;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.employee.dto.UpdateEmployeeRequest;
import temp.unipeople.feature.employee.service.EmployeeService;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  /** Пагинация с total */
  @GetMapping
  public ResponseEntity<Page<EmployeeDto>> findAll(Pageable pageable) {
    Page<EmployeeDto> employees = employeeService.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(employees.getTotalElements()));
    return new ResponseEntity<>(employees, headers, HttpStatus.OK);
  }

  /**
   * Бесконечная прокрутка (без total). Курсор — по createdAt (Instant). TODO: вернуть
   * Slice<EmployeeResponseDTO> и вычислять nextCursor как createdAt последнего элемента.
   */
  @GetMapping("/stream")
  public ResponseEntity<Map<String, Object>> stream(
      @RequestParam(required = false) Instant cursor, @RequestParam(defaultValue = "20") int size) {
    Map<String, Object> body = employeeService.stream(cursor, size);
    return ResponseEntity.ok(body);
  }

  /** Просмотр сотрудника по id. */
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(employeeService.get(id));
  }

  /** Создание сотрудника. */
  @PostMapping
  public ResponseEntity<EmployeeDto> create(@RequestBody CreateEmployeeRequest body) {
    var created = employeeService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Полное обновление (заменяет все изменяемые поля). */
  @PutMapping("/{id}")
  public ResponseEntity<EmployeeDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateEmployeeRequest body) {
    return ResponseEntity.ok(employeeService.update(id, body));
  }

  /** Удаление сотрудника. */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    employeeService.delete(id);
    return ResponseEntity.noContent().build();
  }

  /** Доменная операция: уволить (status = FIRED). */
  @PostMapping("/{id}/fire")
  public ResponseEntity<EmployeeDto> fire(@PathVariable UUID id) {
    return ResponseEntity.ok(employeeService.fire(id));
  }

  /** Доменная операция: активировать (status = ACTIVE). */
  @PostMapping("/{id}/activate")
  public ResponseEntity<Object> activate(@PathVariable UUID id) {
    return ResponseEntity.ok(employeeService.activate(id));
  }
}
