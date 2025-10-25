package com.khasanshin.leaveservice.controller;

import com.khasanshin.leaveservice.dto.CreateLeaveTypeDto;
import com.khasanshin.leaveservice.dto.LeaveTypeDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveTypeDto;
import com.khasanshin.leaveservice.service.LeaveService;
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
import reactor.core.publisher.Mono;

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
  public Mono<ResponseEntity<Page<LeaveTypeDto>>> list(Pageable p) {
    return service.listTypes(p)
            .map(page -> {
              var h = new HttpHeaders();
              h.add("X-Total-Count", String.valueOf(page.getTotalElements()));
              return new ResponseEntity<>(page, h, HttpStatus.OK);
            });
  }

  @Operation(summary = "Создать тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public Mono<ResponseEntity<LeaveTypeDto>> create(@Valid @RequestBody CreateLeaveTypeDto body) {
    return service.createType(body).map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
  }

  @Operation(summary = "Обновить тип отпуска")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PatchMapping("/{id}")
  public Mono<ResponseEntity<LeaveTypeDto>> update(
          @PathVariable("id") UUID id, @Valid @RequestBody UpdateLeaveTypeDto body) {
    return service.updateType(id, body).map(ResponseEntity::ok);
  }

  @Operation(summary = "Удалить тип отпуска")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404"),
    @ApiResponse(responseCode = "409", description = "Есть связанные заявки")
  })
  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> delete(@PathVariable("id") UUID id) {
    return service.deleteType(id).thenReturn(ResponseEntity.noContent().build());
  }
}
