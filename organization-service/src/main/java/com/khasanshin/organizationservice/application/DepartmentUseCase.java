package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentUseCase {

    DepartmentDto get(UUID id);

    DepartmentDto create(CreateDepartmentDto dto);

    DepartmentDto update(UUID id, UpdateDepartmentDto dto);

    void delete(UUID id);

    DepartmentDto setHead(UUID deptId, UUID employeeId);

    Page<DepartmentDto> findAll(Pageable pageable);

    void removeHead(UUID deptId);

    boolean exists(UUID id);

    int clearHeadByEmployee(UUID employeeId);
}
