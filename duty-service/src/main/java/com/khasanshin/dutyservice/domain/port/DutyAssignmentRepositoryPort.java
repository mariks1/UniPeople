package com.khasanshin.dutyservice.domain.port;

import com.khasanshin.dutyservice.domain.model.DutyAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DutyAssignmentRepositoryPort {

    Page<DutyAssignment> findByDepartmentId(UUID departmentId, Pageable pageable);

    Page<DutyAssignment> findByDutyId(UUID dutyId, Pageable pageable);

    Optional<DutyAssignment> findById(UUID id);

    Optional<DutyAssignment> findByIdAndDepartmentId(UUID id, UUID departmentId);

    DutyAssignment save(DutyAssignment assignment);

    void delete(DutyAssignment assignment);

    void deleteById(UUID id);

    boolean existsByDepartmentIdAndEmployeeIdAndDutyId(UUID departmentId, UUID employeeId, UUID dutyId);
}
