package com.khasanshin.leaveservice.application;

import com.khasanshin.leaveservice.dto.CreateLeaveTypeDto;
import com.khasanshin.leaveservice.dto.LeaveTypeDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveTypeDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveTypeUseCase {
    Mono<Long> countTypes();

    Flux<LeaveTypeDto> listTypes(Pageable p);

    Mono<LeaveTypeDto> createType(CreateLeaveTypeDto dto);

    Mono<LeaveTypeDto> updateType(UUID id, UpdateLeaveTypeDto dto);

    Mono<Void> deleteType(UUID id);
}
