package com.khasanshin.leaveservice.infrastructure.persistence;

import com.khasanshin.leaveservice.domain.model.LeaveType;
import com.khasanshin.leaveservice.domain.port.LeaveTypeRepositoryPort;
import com.khasanshin.leaveservice.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class R2dbcLeaveTypeRepositoryAdapter implements LeaveTypeRepositoryPort {

    private final LeaveTypeRepository repository;

    @Override
    public Mono<Boolean> existsByCodeIgnoreCase(String code) {
        return repository.existsByCodeIgnoreCase(code);
    }

    @Override
    public Mono<LeaveType> save(LeaveType type) {
        return repository.save(toEntity(type)).map(this::toDomain);
    }

    @Override
    public Mono<LeaveType> update(LeaveType type) {
        return repository.save(toEntity(type)).map(this::toDomain);
    }

    @Override
    public Mono<LeaveType> findById(java.util.UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteById(java.util.UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Long> count() {
        return repository.count();
    }

    @Override
    public Flux<LeaveType> findAll(Pageable pageable) {
        return repository.findAllBy(pageable).map(this::toDomain);
    }

    private LeaveType toDomain(com.khasanshin.leaveservice.entity.LeaveType e) {
        return LeaveType.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .paid(e.getPaid())
                .maxDaysPerYear(e.getMaxDaysPerYear())
                .build();
    }

    private com.khasanshin.leaveservice.entity.LeaveType toEntity(LeaveType e) {
        return com.khasanshin.leaveservice.entity.LeaveType.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .paid(e.getPaid())
                .maxDaysPerYear(e.getMaxDaysPerYear())
                .build();
    }
}
