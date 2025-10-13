package temp.unipeople.feature.leave.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "LeaveRequest", description = "Заявки на отпуск: создание, правка, согласование")
public class LeaveRequestController {

  private final LeaveService service;

  @Operation(summary = "Получить заявку на отпуск по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  public ResponseEntity<LeaveRequestDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @Operation(summary = "Создать заявку на отпуск")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "400", description = "Неверные даты/пересечения/лимит"),
    @ApiResponse(responseCode = "404", description = "Тип не найден")
  })
  @PostMapping
  public ResponseEntity<LeaveRequestDto> create(@Valid @RequestBody CreateLeaveRequestDto body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(body));
  }

  @Operation(summary = "Обновить заявку (DRAFT/PENDING)")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PatchMapping("/{id}")
  public ResponseEntity<LeaveRequestDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveRequestDto body) {
    return ResponseEntity.ok(service.update(id, body));
  }

  @Operation(summary = "Одобрить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/approve")
  public ResponseEntity<LeaveRequestDto> approve(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return ResponseEntity.ok(service.approve(id, body));
  }

  @Operation(summary = "Отклонить заявку")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/reject")
  public ResponseEntity<LeaveRequestDto> reject(
      @PathVariable("id") UUID id, @Valid @RequestBody DecisionDto body) {
    return ResponseEntity.ok(service.reject(id, body));
  }

  @Operation(summary = "Отменить заявку (PENDING/APPROVED)")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "400"),
    @ApiResponse(responseCode = "404")
  })
  @PostMapping("/{id}/cancel")
  public ResponseEntity<LeaveRequestDto> cancel(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.cancel(id));
  }

  @Operation(summary = "Заявки сотрудника (пагинация)")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping("/by-employee/{employeeId}")
  public ResponseEntity<Page<LeaveRequestDto>> byEmployee(
      @PathVariable("employeeId") UUID employeeId, Pageable p) {
    var page = service.listByEmployee(employeeId, p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }

  @Operation(summary = "Заявки по статусу (пагинация)")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  public ResponseEntity<Page<LeaveRequestDto>> byStatus(
      @RequestParam("status") LeaveRequest.Status status, Pageable p) {
    var page = service.listByStatus(status, p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }
}
