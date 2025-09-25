package temp.unipeople.feature.position.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/positions")
public class PositionController {

  // TODO: инжект сервиса

  @GetMapping
  public ResponseEntity<Page<Object>> findAll(Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }

  /** CRUD */
  @GetMapping("/{id}")
  public ResponseEntity<Object> get(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @PostMapping
  public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Object> update(
      @PathVariable UUID id, @RequestBody Map<String, Object> body) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
  }

  /** Сотрудники на этой должности (текущие), по employment */
  @GetMapping("/{id}/employees")
  public ResponseEntity<Page<Object>> listEmployees(@PathVariable UUID id, Pageable pageable) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", "0");
    return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
  }
}
