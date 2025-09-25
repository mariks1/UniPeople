package temp.unipeople.feature.faculty.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.faculty.entity.Faculty;
import temp.unipeople.feature.faculty.service.FacultyService;

@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
public class FacultyController {
  private final FacultyService service;

  @GetMapping
  public ResponseEntity<Page<Faculty>> findAll(Pageable pageable) {
    var page = service.page(pageable);
    var headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Faculty> get(@PathVariable UUID id) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // TODO
  }

  @PostMapping
  public ResponseEntity<Faculty> create(@RequestBody Faculty req) {
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // TODO
  }
}
