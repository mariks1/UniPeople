package com.khasanshin.leaveservice.application;

import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import com.khasanshin.leaveservice.dto.CreateLeaveRequestDto;
import com.khasanshin.leaveservice.dto.DecisionDto;
import com.khasanshin.leaveservice.dto.LeaveRequestDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveRequestDto;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestUseCase {

    Mono<LeaveRequestDto> get(UUID id);

    Mono<LeaveRequestDto> create(CreateLeaveRequestDto dto);

    Mono<LeaveRequestDto> update(UUID id, UpdateLeaveRequestDto dto);

    Mono<LeaveRequestDto> approve(UUID id, DecisionDto d);

    Mono<LeaveRequestDto> reject(UUID id, DecisionDto d);

    Mono<LeaveRequestDto> cancel(UUID id);

    Mono<Long> countLeaveByEmployee(UUID empId);

    Flux<LeaveRequestDto> listByEmployee(UUID empId, Pageable p);

    Mono<Long> countLeaveByStatus(LeaveRequest.Status status);

    Flux<LeaveRequestDto> listByStatus(LeaveRequest.Status status, Pageable p);
}
