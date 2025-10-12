package temp.unipeople.feature.department.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.department.dto.CreateDepartmentDto;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.department.dto.UpdateDepartmentDto;
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
  public ResponseEntity<DepartmentDto> create(@Valid @RequestBody CreateDepartmentDto body) {
    var created = service.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Обновить департамент (имя, код, привязка к факультету) */
  @PutMapping("/{id}")
  public ResponseEntity<DepartmentDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateDepartmentDto body) {
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

  // TODO
  /** Список сотрудников кафедры (текущие) — пагинация + total */
  @GetMapping("/{id}/employees")
  public ResponseEntity<Page<Object>> listEmployees(@PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }
}
