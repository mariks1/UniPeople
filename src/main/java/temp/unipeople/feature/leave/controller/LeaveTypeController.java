package temp.unipeople.feature.leave.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.leave.dto.CreateLeaveTypeDto;
import temp.unipeople.feature.leave.dto.LeaveTypeDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveTypeDto;
import temp.unipeople.feature.leave.service.LeaveService;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
public class LeaveTypeController {

  private final LeaveService service;

  @GetMapping
  public ResponseEntity<Page<LeaveTypeDto>> list(Pageable p) {
    var page = service.listTypes(p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<LeaveTypeDto> create(@Valid @RequestBody CreateLeaveTypeDto body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createType(body));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<LeaveTypeDto> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateLeaveTypeDto body) {
    return ResponseEntity.ok(service.updateType(id, body));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    service.deleteType(id);
    return ResponseEntity.noContent().build();
  }
}
