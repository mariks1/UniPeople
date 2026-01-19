package com.khasanshin.leaveservice.domain.port;

import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestRepositoryPort {

    Mono<LeaveRequest> findById(UUID id);

    Mono<LeaveRequest> save(LeaveRequest leaveRequest);

    Mono<Boolean> existsById(UUID id);

    Mono<Boolean> existsOverlaps(UUID employeeId, LocalDate from, LocalDate to);

    Mono<Boolean> existsOverlapsExcluding(UUID id, UUID employeeId, LocalDate from, LocalDate to);

    Mono<Integer> sumApprovedDaysForYear(UUID employeeId, UUID typeId, int year);

    Flux<LeaveRequest> findByEmployeeIdOrderByDateFromDesc(UUID employeeId, Pageable pageable);
    Mono<Long> countByEmployeeId(UUID employeeId);

    Flux<LeaveRequest> findByStatus(LeaveRequest.Status status, Pageable pageable);
    Mono<Long> countByStatus(LeaveRequest.Status status);
}
