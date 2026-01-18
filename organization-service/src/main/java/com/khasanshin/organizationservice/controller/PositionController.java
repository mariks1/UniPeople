package com.khasanshin.organizationservice.controller;

import com.khasanshin.organizationservice.application.PositionUseCase;
import com.khasanshin.organizationservice.dto.CreatePositionDto;
import com.khasanshin.organizationservice.dto.PositionDto;
import com.khasanshin.organizationservice.dto.UpdatePositionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@Tag(name = "Position", description = "Справочник должностей")
public class PositionController {

  private final PositionUseCase positionService;

  @Operation(
      summary = "Список должностей (пагинация)",
      description = "Фильтрация по части названия через параметр q. X-Total-Count в заголовке.")
  @Parameters(
      @Parameter(name = "q", description = "Подстрока в названии (contains, case-insensitive)"))
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее количество записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<PositionDto>> findAll(
      @RequestParam(name = "q", required = false) String q, Pageable pageable) {
    Page<PositionDto> page = positionService.findAll(q, pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @Operation(summary = "Получить должность по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<PositionDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(positionService.get(id));
  }

  @Operation(summary = "Создать должность")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Название уже существует")
  })
  @PostMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<PositionDto> create(@Valid @RequestBody CreatePositionDto body) {
    PositionDto created = positionService.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить должность")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности")
  })
  @PutMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<PositionDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdatePositionDto body) {
    return ResponseEntity.ok(positionService.update(id, body));
  }

  @Operation(summary = "Удалить должность")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404"),
    @ApiResponse(responseCode = "409", description = "Есть связанные назначения")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    positionService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Позиция существует"),
          @ApiResponse(responseCode = "404", description = "Позиция не найдена")
  })
  @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> head(@PathVariable("id") UUID id) {
    return positionService.exists(id)
            ? ResponseEntity.ok().build()
            : ResponseEntity.notFound().build();
  }

}
