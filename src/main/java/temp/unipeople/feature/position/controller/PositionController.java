package temp.unipeople.feature.position.controller;

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
import org.springframework.web.bind.annotation.*;
import temp.unipeople.feature.position.dto.CreatePositionDto;
import temp.unipeople.feature.position.dto.PositionDto;
import temp.unipeople.feature.position.dto.UpdatePositionDto;
import temp.unipeople.feature.position.service.PositionService;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
@Tag(name = "Position", description = "Справочник должностей")
public class PositionController {

  private final PositionService positionService;

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
  public ResponseEntity<PositionDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(positionService.get(id));
  }

  @Operation(summary = "Создать должность")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Название уже существует")
  })
  @PostMapping
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
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    positionService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
