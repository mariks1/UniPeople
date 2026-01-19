package com.khasanshin.employmentservice.application;

import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmploymentUseCase {

    Mono<EmploymentDto> get(UUID id);

    Mono<EmploymentDto> create(CreateEmploymentDto dto);

    Mono<EmploymentDto> update(UUID id, UpdateEmploymentDto dto);

    Mono<EmploymentDto> close(UUID id, CloseEmploymentDto dto);

    Flux<EmploymentDto> listByEmployee(UUID employeeId, int page, int size);

    Mono<Long> countByEmployee(UUID employeeId);

    Flux<EmploymentDto> listByDepartment(UUID departmentId, boolean active, int page, int size);

    Mono<Long> countByDepartment(UUID departmentId, boolean active);
}
