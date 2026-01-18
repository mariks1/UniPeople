package com.khasanshin.dutyservice.infrastructure.persistence;

import com.khasanshin.dutyservice.domain.model.DutyAssignment;
import com.khasanshin.dutyservice.domain.port.DutyAssignmentRepositoryPort;
import com.khasanshin.dutyservice.repository.DutyAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JpaDutyAssignmentRepositoryAdapter implements DutyAssignmentRepositoryPort {

    private final DutyAssignmentRepository repository;

    @Override
    public Page<DutyAssignment> findByDepartmentId(UUID departmentId, Pageable pageable) {
        return repository.findByDepartmentId(departmentId, pageable).map(this::toDomain);
    }

    @Override
    public Page<DutyAssignment> findByDutyId(UUID dutyId, Pageable pageable) {
        return repository.findByDutyId(dutyId, pageable).map(this::toDomain);
    }

    @Override
    public Optional<DutyAssignment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<DutyAssignment> findByIdAndDepartmentId(UUID id, UUID departmentId) {
        return repository.findByIdAndDepartmentId(id, departmentId).map(this::toDomain);
    }

    @Override
    public DutyAssignment save(DutyAssignment assignment) {
        com.khasanshin.dutyservice.entity.DepartmentDutyAssignment saved =
                repository.saveAndFlush(toEntity(assignment));
        return toDomain(saved);
    }

    @Override
    public void delete(DutyAssignment assignment) {
        if (assignment.getId() != null) {
            repository.deleteById(assignment.getId());
        }
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByDepartmentIdAndEmployeeIdAndDutyId(UUID departmentId, UUID employeeId, UUID dutyId) {
        return repository.existsByDepartmentIdAndEmployeeIdAndDutyId(departmentId, employeeId, dutyId);
    }

    private DutyAssignment toDomain(com.khasanshin.dutyservice.entity.DepartmentDutyAssignment e) {
        return DutyAssignment.builder()
                .id(e.getId())
                .departmentId(e.getDepartmentId())
                .employeeId(e.getEmployeeId())
                .dutyId(e.getDutyId())
                .assignedBy(e.getAssignedBy())
                .assignedAt(e.getAssignedAt())
                .note(e.getNote())
                .build();
    }

    private com.khasanshin.dutyservice.entity.DepartmentDutyAssignment toEntity(DutyAssignment a) {
        return com.khasanshin.dutyservice.entity.DepartmentDutyAssignment.builder()
                .id(a.getId())
                .departmentId(a.getDepartmentId())
                .employeeId(a.getEmployeeId())
                .dutyId(a.getDutyId())
                .assignedBy(a.getAssignedBy())
                .assignedAt(a.getAssignedAt())
                .note(a.getNote())
                .build();
    }
}
