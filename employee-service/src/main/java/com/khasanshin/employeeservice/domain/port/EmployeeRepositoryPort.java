package com.khasanshin.employeeservice.domain.port;

import com.khasanshin.employeeservice.domain.model.Employee;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface EmployeeRepositoryPort {

    boolean existsById(UUID id);

    Employee save(Employee employee);

    Optional<Employee> findById(UUID id);

    void deleteById(UUID id);

    Page<Employee> findAll(Pageable pageable);

    Slice<Employee> findAllBy(Pageable pageable);

    Slice<Employee> findByCreatedAtLessThan(Instant cursor, Pageable pageable);
}
