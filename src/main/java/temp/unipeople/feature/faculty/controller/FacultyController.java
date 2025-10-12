package temp.unipeople.feature.faculty.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.faculty.dto.CreateFacultyDto;
import temp.unipeople.feature.faculty.dto.FacultyDto;
import temp.unipeople.feature.faculty.dto.UpdateFacultyDto;
import temp.unipeople.feature.faculty.service.FacultyService;

@Slf4j
@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {

  private final FacultyService service;

  @GetMapping("/{id}")
  public ResponseEntity<FacultyDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @PostMapping
  public ResponseEntity<FacultyDto> create(@RequestBody CreateFacultyDto dto) {
    var saved = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @PutMapping("/{id}")
  public ResponseEntity<FacultyDto> update(
      @PathVariable("id") UUID id, @RequestBody UpdateFacultyDto body) {
    return ResponseEntity.ok(service.update(id, body));
  }
}
