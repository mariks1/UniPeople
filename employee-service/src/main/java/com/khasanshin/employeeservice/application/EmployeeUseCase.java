package com.khasanshin.employeeservice.application;

import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeUseCase {

    boolean exists(UUID id);

    EmployeeDto create(CreateEmployeeDto dto);

    EmployeeDto update(UUID id, UpdateEmployeeDto dto);

    EmployeeDto get(UUID id);

    EmployeeDto fire(UUID id);

    void delete(UUID id);

    Page<EmployeeDto> findAll(Pageable pageable);

    Map<String, Object> stream(Instant cursor, int size);

    EmployeeDto activate(UUID id);
}
