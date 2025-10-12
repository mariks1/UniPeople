package temp.unipeople.feature.position.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.position.dto.CreatePositionDto;
import temp.unipeople.feature.position.dto.PositionDto;
import temp.unipeople.feature.position.dto.UpdatePositionDto;
import temp.unipeople.feature.position.service.PositionService;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

  private final PositionService positionService;

  @GetMapping
  public ResponseEntity<Page<PositionDto>> findAll(
      @RequestParam(required = false) String q, Pageable pageable) {
    Page<PositionDto> page = positionService.findAll(q, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  /** CRUD */
  @GetMapping("/{id}")
  public ResponseEntity<PositionDto> get(@PathVariable UUID id) {
    return ResponseEntity.ok(positionService.get(id));
  }

  @PostMapping
  public ResponseEntity<PositionDto> create(@Valid @RequestBody CreatePositionDto body) {
    PositionDto created = positionService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<PositionDto> update(
      @PathVariable UUID id, @Valid @RequestBody UpdatePositionDto body) {
    return ResponseEntity.ok(positionService.update(id, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    positionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
