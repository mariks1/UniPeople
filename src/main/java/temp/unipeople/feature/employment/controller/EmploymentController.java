package temp.unipeople.feature.employment.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employments")
public class EmploymentController {

    // TODO: инжект сервиса

    /** Получить назначение по id */
    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Создать назначение (приём/перевод): employeeId, departmentId, positionId, startDate, rate, salary */
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Обновить назначение (изменить оклад/ставку/дату окончания) */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Закрыть назначение (установить endDate, статус CLOSED) */
    @PostMapping("/{id}/close")
    public ResponseEntity<Object> close(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** История назначений сотрудника (пагинация + total) */
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<Page<Object>> listByEmployee(@PathVariable UUID employeeId, Pageable pageable) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "0");
        return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
    }

    /** Текущие назначения по департаменту (active=true по умолчанию) */
    @GetMapping("/by-department/{departmentId}")
    public ResponseEntity<Page<Object>> listByDepartment(
            @PathVariable UUID departmentId,
            @RequestParam(required = false, defaultValue = "true") boolean active,
            Pageable pageable) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "0");
        return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
    }
}

