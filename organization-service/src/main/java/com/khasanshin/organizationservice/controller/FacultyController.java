package com.khasanshin.organizationservice.controller;

import com.khasanshin.organizationservice.dto.CreateFacultyDto;
import com.khasanshin.organizationservice.dto.FacultyDto;
import com.khasanshin.organizationservice.dto.UpdateFacultyDto;
import com.khasanshin.organizationservice.service.FacultyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/faculties")
@RequiredArgsConstructor
@Tag(name = "Faculty", description = "Факультеты: CRUD")
public class FacultyController {

  private final FacultyService service;

  @Operation(summary = "Получить факультет по ID")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @GetMapping("/{id}")
  public ResponseEntity<FacultyDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @Operation(summary = "Создать факультет")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  public ResponseEntity<FacultyDto> create(@Valid @RequestBody CreateFacultyDto dto) {
    var saved = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
  }

  @Operation(summary = "Обновить факультет")
  @ApiResponses({@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
  @PutMapping("/{id}")
  public ResponseEntity<FacultyDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateFacultyDto body) {
    return ResponseEntity.ok(service.update(id, body));
  }
}
