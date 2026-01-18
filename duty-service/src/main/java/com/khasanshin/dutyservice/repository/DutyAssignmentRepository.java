package com.khasanshin.dutyservice.repository;

import java.util.Optional;
import java.util.UUID;

import com.khasanshin.dutyservice.entity.DepartmentDutyAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DutyAssignmentRepository extends JpaRepository<DepartmentDutyAssignment, UUID> {

  Page<DepartmentDutyAssignment> findByDepartmentId(UUID departmentId, Pageable pageable);

  Page<DepartmentDutyAssignment> findByDutyId(UUID dutyId, Pageable pageable);

  Optional<DepartmentDutyAssignment> findByIdAndDepartmentId(UUID id, UUID departmentId);

  boolean existsByDepartmentIdAndEmployeeIdAndDutyId(
      UUID departmentId, UUID employeeId, UUID dutyId);
}
