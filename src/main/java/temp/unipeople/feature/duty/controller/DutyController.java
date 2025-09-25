package temp.unipeople.feature.duty.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/duties")
public class DutyController {

  // TODO: сервис заинжектить

  /** Пагинация + total */
  @GetMapping
  public ResponseEntity<Page<Object>> findAll(Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

  /** Получить обязанность */
  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Создать обязанность */
  @PostMapping
  public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Обновить обязанность */
  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable UUID id, @RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Удалить обязанность */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Список назначений по конкретной обязанности (для аналитики/поиска) */
  @GetMapping("/{id}/assignments")
  public ResponseEntity<Page<Object>> listAssignments(@PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }
}
