package com.khasanshin.organizationservice.controller;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
@Tag(name = "Department", description = "Кафедры/департаменты (CRUD, назначение заведующего)")
public class DepartmentController {

  private final DepartmentService service;

  @Operation(
      summary = "Список департаментов (пагинация)",
      description =
          "Возвращает страницу департаментов. Общее количество — в заголовке **X-Total-Count**.")
  @ApiResponse(
      responseCode = "200",
      headers =
          @Header(
              name = "X-Total-Count",
              description = "Общее число записей",
              schema = @Schema(type = "integer")))
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<DepartmentDto>> findAll(
          @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
    Page<DepartmentDto> page = service.findAll(pageable);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Total-Count", String.valueOf(page.getTotalElements()));
    return new ResponseEntity<>(page, headers, HttpStatus.OK);
  }

  @Operation(summary = "Получить департамент по id")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Не найден")
  })
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<DepartmentDto> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(service.get(id));
  }

  @Operation(summary = "Создать департамент")
  @ApiResponses({
    @ApiResponse(responseCode = "201"),
    @ApiResponse(responseCode = "409", description = "Конфликт уникальности (code)")
  })
  @PostMapping
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<DepartmentDto> create(@Valid @RequestBody CreateDepartmentDto body) {
    var created = service.create(body);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @Operation(summary = "Обновить департамент")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Не найден")
  })
  @PutMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<DepartmentDto> update(
      @PathVariable("id") UUID id, @Valid @RequestBody UpdateDepartmentDto body) {
    return ResponseEntity.ok(service.update(id, body));
  }

  @Operation(summary = "Удалить департамент")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404", description = "Не найден"),
    @ApiResponse(responseCode = "409", description = "Есть связанные данные")
  })
  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Назначить/сменить заведующего кафедрой")
  @ApiResponses({
    @ApiResponse(responseCode = "200"),
    @ApiResponse(responseCode = "404", description = "Департамент или сотрудник не найден")
  })
  @PutMapping("/{id}/head/{employeeId}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<DepartmentDto> setHead(
      @PathVariable("id") UUID id, @PathVariable("employeeId") UUID employeeId) {
    DepartmentDto dto = service.setHead(id, employeeId);
    return ResponseEntity.ok(dto);
  }

  @Operation(summary = "Снять заведующего кафедрой")
  @ApiResponses({
    @ApiResponse(responseCode = "204"),
    @ApiResponse(responseCode = "404", description = "Департамент не найден")
  })
  @DeleteMapping("/{id}/head")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<Void> removeHead(@PathVariable("id") UUID id) {
    service.removeHead(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
          summary = "Проверить существование департамента",
          description = "HEAD-запрос без тела. Возвращает 200, если департамент существует, иначе 404."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Департамент существует"),
          @ApiResponse(responseCode = "404", description = "Департамент не найден")
  })
  @RequestMapping(method = RequestMethod.HEAD, value = "/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> head(@PathVariable("id") UUID id) {
    return service.exists(id)
            ? ResponseEntity.ok().build()
            : ResponseEntity.notFound().build();
  }

  @Operation(
          summary = "Снять заведующего по employeeId во всех департаментах",
          description = "Удаляет headEmployee там, где он равен заданному employeeId"
  )
  @ApiResponses({
          @ApiResponse(responseCode = "204", description = "Готово")
  })
  @DeleteMapping("/head/by-employee/{employeeId}")
  @PreAuthorize("@perm.hasAny(authentication,'ORG_ADMIN')")
  public ResponseEntity<Void> clearHeadByEmployee(@PathVariable("employeeId") UUID employeeId) {
    int affected = service.clearHeadByEmployee(employeeId);
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Affected-Count", String.valueOf(affected));
    return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
  }

}
