package com.khasanshin.leaveservice.domain.port;

import com.khasanshin.leaveservice.domain.model.LeaveType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveTypeRepositoryPort {

    Mono<Boolean> existsByCodeIgnoreCase(String code);

    Mono<LeaveType> save(LeaveType type);

    Mono<LeaveType> update(LeaveType type);

    Mono<LeaveType> findById(UUID id);

    Mono<Void> deleteById(UUID id);

    Mono<Long> count();

    Flux<LeaveType> findAll(Pageable pageable);
}
