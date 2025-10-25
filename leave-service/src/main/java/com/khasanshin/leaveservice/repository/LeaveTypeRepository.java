package com.khasanshin.leaveservice.repository;

import java.util.UUID;

import com.khasanshin.leaveservice.entity.LeaveType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveTypeRepository extends R2dbcRepository<LeaveType, UUID> {
  Mono<Boolean> existsByCodeIgnoreCase(String code);

  Flux<LeaveType> findAllBy(Pageable pageable);
  Mono<Long> count();

}
