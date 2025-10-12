package temp.unipeople.feature.employment.controller;

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
public class EmploymentController {

  private final EmploymentService employmentService;

  /** Получить назначение по id */
  @GetMapping("/{id}")
  public ResponseEntity<EmploymentDto> get(@PathVariable UUID id) {
    return ResponseEntity.ok(employmentService.get(id));
  }

  /**
   * Создать назначение (приём/перевод): employeeId, departmentId, positionId, startDate, rate,
   * salary
   */
  @PostMapping
  public ResponseEntity<EmploymentDto> create(@RequestBody CreateEmploymentDto body) {
    EmploymentDto dto = employmentService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  /** Обновить назначение (изменить оклад/ставку/дату окончания) */
  @PutMapping("/{id}")
  public ResponseEntity<EmploymentDto> update(
      @PathVariable UUID id, @RequestBody UpdateEmploymentDto body) {
    return ResponseEntity.ok(employmentService.update(id, body));
  }

  /** Закрыть назначение (установить endDate, статус CLOSED) */
  @PostMapping("/{id}/close")
  public ResponseEntity<EmploymentDto> close(
      @PathVariable UUID id, @RequestBody(required = false) CloseEmploymentDto body) {
    return ResponseEntity.ok(employmentService.close(id, body));
  }

  /** История назначений сотрудника (пагинация + total) */
  @GetMapping("/by-employee/{employeeId}")
  public ResponseEntity<Page<EmploymentDto>> listByEmployee(
      @PathVariable UUID employeeId, Pageable pageable) {
    Page<EmploymentDto> page = employmentService.listByEmployee(employeeId, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  /** Текущие назначения по департаменту (active=true по умолчанию) */
  @GetMapping("/by-department/{departmentId}")
  public ResponseEntity<Page<EmploymentDto>> listByDepartment(
      @PathVariable UUID departmentId,
      @RequestParam(required = false, defaultValue = "true") boolean active,
      Pageable pageable) {
    Page<EmploymentDto> page = employmentService.listByDepartment(departmentId, active, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }
}
