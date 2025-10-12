package temp.unipeople.feature.leave.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.leave.dto.CreateLeaveRequestDto;
import temp.unipeople.feature.leave.dto.DecisionDto;
import temp.unipeople.feature.leave.dto.LeaveRequestDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveRequestDto;
import temp.unipeople.feature.leave.entity.LeaveRequest;
import temp.unipeople.feature.leave.service.LeaveService;

@RestController
@RequestMapping("/api/v1/leave-requests")
@RequiredArgsConstructor
public class LeaveRequestController {

  private final LeaveService service;

  @GetMapping("/{id}")
  public ResponseEntity<LeaveRequestDto> get(@PathVariable UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @PostMapping
  public ResponseEntity<LeaveRequestDto> create(@Valid @RequestBody CreateLeaveRequestDto body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<LeaveRequestDto> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateLeaveRequestDto body) {
    return ResponseEntity.ok(service.update(id, body));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<LeaveRequestDto> approve(
      @PathVariable UUID id, @Valid @RequestBody DecisionDto body) {
    return ResponseEntity.ok(service.approve(id, body));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<LeaveRequestDto> reject(
      @PathVariable UUID id, @Valid @RequestBody DecisionDto body) {
    return ResponseEntity.ok(service.reject(id, body));
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<LeaveRequestDto> cancel(@PathVariable UUID id) {
    return ResponseEntity.ok(service.cancel(id));
  }

  @GetMapping("/by-employee/{employeeId}")
  public ResponseEntity<Page<LeaveRequestDto>> byEmployee(
      @PathVariable UUID employeeId, Pageable p) {
    var page = service.listByEmployee(employeeId, p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }

  @GetMapping
  public ResponseEntity<Page<LeaveRequestDto>> byStatus(
      @RequestParam LeaveRequest.Status status, Pageable p) {
    var page = service.listByStatus(status, p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }
}
