package com.khasanshin.employmentservice.infrastructure.persistence;

import com.khasanshin.employmentservice.domain.model.Employment;
import com.khasanshin.employmentservice.domain.port.EmploymentRepositoryPort;
import com.khasanshin.employmentservice.repository.EmploymentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaEmploymentRepositoryAdapter implements EmploymentRepositoryPort {

    private final EmploymentRepository repository;

    @Override
    public Optional<Employment> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Employment save(Employment employment) {
        return toDomain(repository.saveAndFlush(toEntity(employment)));
    }

    @Override
    public Page<Employment> findByEmployeeIdOrderByStartDateDesc(UUID employeeId, Pageable pageable) {
        return repository.findByEmployeeIdOrderByStartDateDesc(employeeId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Employment> findByDepartmentId(UUID departmentId, Pageable pageable) {
        return repository.findByDepartmentId(departmentId, pageable).map(this::toDomain);
    }

    @Override
    public Page<Employment> findByDepartmentIdAndStatus(UUID departmentId, Employment.Status status, Pageable pageable) {
        return repository.findByDepartmentIdAndStatus(departmentId, toEntityStatus(status), pageable).map(this::toDomain);
    }

    @Override
    public List<Employment> findOverlaps(UUID employeeId, UUID departmentId, UUID positionId, LocalDate start, LocalDate end) {
        return repository.findOverlaps(employeeId, departmentId, positionId, start, end).stream().map(this::toDomain).toList();
    }

    @Override
    public long countByEmployeeId(UUID employeeId) {
        return repository.countByEmployeeId(employeeId);
    }

    @Override
    public long countByDepartmentId(UUID departmentId) {
        return repository.countByDepartmentId(departmentId);
    }

    @Override
    public long countByDepartmentIdAndStatus(UUID departmentId, Employment.Status status) {
        return repository.countByDepartmentIdAndStatus(departmentId, toEntityStatus(status));
    }

    private Employment toDomain(com.khasanshin.employmentservice.entity.Employment e) {
        return Employment.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .departmentId(e.getDepartmentId())
                .positionId(e.getPositionId())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .rate(e.getRate())
                .salary(e.getSalary())
                .status(Employment.Status.valueOf(e.getStatus().name()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private com.khasanshin.employmentservice.entity.Employment toEntity(Employment e) {
        com.khasanshin.employmentservice.entity.Employment ent = com.khasanshin.employmentservice.entity.Employment.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .departmentId(e.getDepartmentId())
                .positionId(e.getPositionId())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .rate(e.getRate())
                .salary(e.getSalary())
                .status(toEntityStatus(e.getStatus()))
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
        return ent;
    }

    private com.khasanshin.employmentservice.entity.Employment.Status toEntityStatus(Employment.Status status) {
        return com.khasanshin.employmentservice.entity.Employment.Status.valueOf(status.name());
    }
}
