package temp.unipeople.feature.employee.controller;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.employee.dto.*;
import temp.unipeople.feature.employee.service.EmployeeService;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {
  private final EmployeeService service;

  @GetMapping
  public ResponseEntity<Page<EmployeeResponseDTO>> findAll(Pageable pageable) {
    var page = service.page(pageable);
    var headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @GetMapping("/stream")
  public Map<String, Object> stream(
      @RequestParam(required = false) Instant cursor, @RequestParam(defaultValue = "20") int size) {
    if (size > 50) size = 50;
    var slice = service.slice(cursor, size);
    return Map.of("items", slice.getContent(), "hasNext", slice.hasNext(), "nextCursor", cursor);
  }

  @GetMapping("/{id}")
  public EmployeeResponseDTO get(@PathVariable UUID id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EmployeeResponseDTO create(@RequestBody @Valid EmployeeCreateRequestDTO r) {
    return service.create(r);
  }
}
