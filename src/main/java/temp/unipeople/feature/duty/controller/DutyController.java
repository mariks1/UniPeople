package temp.unipeople.feature.duty.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.duty.dto.CreateDutyDto;
import temp.unipeople.feature.duty.dto.DutyAssignmentDto;
import temp.unipeople.feature.duty.dto.DutyDto;
import temp.unipeople.feature.duty.dto.UpdateDutyDto;
import temp.unipeople.feature.duty.service.DutyAssignmentService;
import temp.unipeople.feature.duty.service.DutyService;

@RestController
@RequestMapping("/api/v1/duties")
@RequiredArgsConstructor
public class DutyController {

  private final DutyService service;
  private final DutyAssignmentService assignmentService;

  /** Пагинация + total */
  @GetMapping
  public ResponseEntity<Page<DutyDto>> findAll(Pageable pageable) {
    Page<DutyDto> page = service.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  /** Получить обязанность */
  @GetMapping("/{id}")
  public ResponseEntity<DutyDto> get(@PathVariable UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  /** Создать обязанность */
  @PostMapping
  public ResponseEntity<DutyDto> create(@Valid @RequestBody CreateDutyDto dto) {
    DutyDto created = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /** Обновить обязанность */
  @PutMapping("/{id}")
  public ResponseEntity<DutyDto> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateDutyDto dto) {
    return ResponseEntity.ok(service.update(id, dto));
  }

  /** Удалить обязанность */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /** Список назначений по конкретной обязанности (для аналитики/поиска) */
  @GetMapping("/{id}/assignments")
  public ResponseEntity<Page<DutyAssignmentDto>> listAssignments(
      @PathVariable UUID id, Pageable pageable) {
    Page<DutyAssignmentDto> page = service.listAssignments(id, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  /** Список назначений обязанностей в департаменте (пагинация + total) */
  @GetMapping("/{id}/duties/assignments")
  public ResponseEntity<Page<DutyAssignmentDto>> listDutyAssignments(
      @PathVariable UUID id, Pageable pageable) {
    Page<DutyAssignmentDto> page = assignmentService.list(id, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }
}
