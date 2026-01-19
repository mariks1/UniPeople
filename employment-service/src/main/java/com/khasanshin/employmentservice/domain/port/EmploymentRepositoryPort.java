package com.khasanshin.employmentservice.domain.port;

import com.khasanshin.employmentservice.domain.model.Employment;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmploymentRepositoryPort {

    Optional<Employment> findById(UUID id);

    Employment save(Employment employment);

    Page<Employment> findByEmployeeIdOrderByStartDateDesc(UUID employeeId, Pageable pageable);

    Page<Employment> findByDepartmentId(UUID departmentId, Pageable pageable);

    Page<Employment> findByDepartmentIdAndStatus(UUID departmentId, Employment.Status status, Pageable pageable);

    List<Employment> findOverlaps(UUID employeeId, UUID departmentId, UUID positionId, LocalDate start, LocalDate end);

    long countByEmployeeId(UUID employeeId);

    long countByDepartmentId(UUID departmentId);

    long countByDepartmentIdAndStatus(UUID departmentId, Employment.Status status);
}
