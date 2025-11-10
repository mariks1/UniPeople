package com.khasanshin.employeeservice.repository;

import java.time.Instant;
import java.util.UUID;

import com.khasanshin.employeeservice.entity.Employee;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
  Slice<Employee> findByCreatedAtLessThan(Instant cursor, Pageable pageable);

  Slice<Employee> findAllBy(Pageable pageable);
}
