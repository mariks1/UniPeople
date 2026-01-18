package com.khasanshin.organizationservice.domain.port;

import com.khasanshin.organizationservice.domain.model.Department;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentRepositoryPort {

    Optional<Department> findById(UUID id);

    Department save(Department department);

    void delete(Department department);

    boolean existsById(UUID id);

    Page<Department> findAll(Pageable pageable);

    int clearHeadByEmployeeId(UUID employeeId);
}
