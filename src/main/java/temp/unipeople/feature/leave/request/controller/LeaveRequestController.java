package temp.unipeople.feature.leave.request.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave-requests")
public class LeaveRequestController {

    // TODO: инжект сервиса

    /** Пагинация заявок с фильтрами: employeeId, status, from, to */
    @GetMapping
    public ResponseEntity<Page<Object>> search(
            @RequestParam(required = false) UUID employeeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            Pageable pageable) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "0");
        return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
    }

    /** Получить заявку по id */
    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Создать заявку (черновик или сразу PENDING) */
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Обновить черновик */
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateDraft(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Отправить на согласование */
    @PostMapping("/{id}/submit")
    public ResponseEntity<Object> submit(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Утвердить заявку (заведующий своей кафедры) */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Object> approve(@PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Отклонить заявку */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Object> reject(@PathVariable UUID id, @RequestBody Map<String, Object> body /* {reason} */) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Отменить свою заявку (пока не согласована) */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Object> cancel(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    /** Заявки конкретного сотрудника */
    @GetMapping("/by-employee/{employeeId}")
    public ResponseEntity<Page<Object>> listByEmployee(@PathVariable UUID employeeId, Pageable pageable) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "0");
        return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
    }

    /** Заявки департамента в ожидании (для заведующего) */
    @GetMapping("/pending/by-department/{departmentId}")
    public ResponseEntity<Page<Object>> pendingForDepartment(@PathVariable UUID departmentId, Pageable pageable) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", "0");
        return new ResponseEntity<>(Page.empty(pageable), headers, HttpStatus.NOT_IMPLEMENTED);
    }
}
