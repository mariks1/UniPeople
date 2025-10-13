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
import temp.unipeople.feature.leave.dto.CreateLeaveTypeDto;
import temp.unipeople.feature.leave.dto.LeaveTypeDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveTypeDto;
import temp.unipeople.feature.leave.service.LeaveService;

@RestController
@RequestMapping("/api/v1/leave-types")
@RequiredArgsConstructor
@Tag(name = "LeaveType", description = "Справочник видов отпусков")
public class LeaveTypeController {

  private final LeaveService service;

  @Operation(
      summary = "Список типов отпусков (пагинация)",
      description = "X-Total-Count в заголовке.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  public ResponseEntity<Page<LeaveTypeDto>> list(Pageable p) {
    var page = service.listTypes(p);
    var h = new HttpHeaders();
    h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, h, HttpStatus.OK);
  }

  @Operation(summary = "Создать тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public ResponseEntity<LeaveTypeDto> create(@Valid @RequestBody CreateLeaveTypeDto body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createType(body));
  }

  @Operation(summary = "Обновить тип отпуска")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PatchMapping("/{id}")
  public ResponseEntity<LeaveTypeDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveTypeDto body) {
    return ResponseEntity.ok(service.updateType(id, body));
  }

  @Operation(summary = "Удалить тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404"),
    @ApiResponse(responseCode = "409", description = "Есть связанные заявки")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.deleteType(id);
    return ResponseEntity.noContent().build();
  }
}
