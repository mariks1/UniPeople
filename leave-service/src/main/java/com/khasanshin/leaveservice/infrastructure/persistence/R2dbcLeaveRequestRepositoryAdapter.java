package com.khasanshin.leaveservice.infrastructure.persistence;

import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import com.khasanshin.leaveservice.domain.port.LeaveRequestRepositoryPort;
import com.khasanshin.leaveservice.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class R2dbcLeaveRequestRepositoryAdapter implements LeaveRequestRepositoryPort {

    private final LeaveRequestRepository repository;

    @Override
    public Mono<LeaveRequest> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<LeaveRequest> save(LeaveRequest leaveRequest) {
        return repository.save(toEntity(leaveRequest)).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Boolean> existsOverlaps(UUID employeeId, LocalDate from, LocalDate to) {
        return repository.existsOverlaps(employeeId, from, to);
    }

    @Override
    public Mono<Boolean> existsOverlapsExcluding(UUID id, UUID employeeId, LocalDate from, LocalDate to) {
        return repository.existsOverlapsExcluding(id, employeeId, from, to);
    }

    @Override
    public Mono<Integer> sumApprovedDaysForYear(UUID employeeId, UUID typeId, int year) {
        return repository.sumApprovedDaysForYear(employeeId, typeId, year);
    }

    @Override
    public Flux<LeaveRequest> findByEmployeeIdOrderByDateFromDesc(UUID employeeId, Pageable pageable) {
        return repository.findByEmployeeIdOrderByDateFromDesc(employeeId, pageable).map(this::toDomain);
    }

    @Override
    public Mono<Long> countByEmployeeId(UUID employeeId) {
        return repository.countByEmployeeId(employeeId);
    }

    @Override
    public Flux<LeaveRequest> findByStatus(LeaveRequest.Status status, Pageable pageable) {
        return repository.findByStatus(com.khasanshin.leaveservice.entity.LeaveRequest.Status.valueOf(status.name()), pageable).map(this::toDomain);
    }

    @Override
    public Mono<Long> countByStatus(LeaveRequest.Status status) {
        return repository.countByStatus(com.khasanshin.leaveservice.entity.LeaveRequest.Status.valueOf(status.name()));
    }

    private LeaveRequest toDomain(com.khasanshin.leaveservice.entity.LeaveRequest e) {
        return LeaveRequest.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .typeId(e.getTypeId())
                .dateFrom(e.getDateFrom())
                .dateTo(e.getDateTo())
                .status(LeaveRequest.Status.valueOf(e.getStatus().name()))
                .approverId(e.getApproverId())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private com.khasanshin.leaveservice.entity.LeaveRequest toEntity(LeaveRequest e) {
        return com.khasanshin.leaveservice.entity.LeaveRequest.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .typeId(e.getTypeId())
                .dateFrom(e.getDateFrom())
                .dateTo(e.getDateTo())
                .status(com.khasanshin.leaveservice.entity.LeaveRequest.Status.valueOf(e.getStatus().name()))
                .approverId(e.getApproverId())
                .comment(e.getComment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
