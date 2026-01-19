package com.khasanshin.organizationservice.infrastructure.persistence;

import com.khasanshin.organizationservice.domain.model.Department;
import com.khasanshin.organizationservice.domain.port.DepartmentRepositoryPort;
import com.khasanshin.organizationservice.repository.DepartmentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaDepartmentRepositoryAdapter implements DepartmentRepositoryPort {

    private final DepartmentRepository repository;

    @Override
    public Optional<Department> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Department save(Department department) {
        return toDomain(repository.save(toEntity(department)));
    }

    @Override
    public void delete(Department department) {
        repository.delete(toEntity(department));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Page<Department> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public int clearHeadByEmployeeId(UUID employeeId) {
        return repository.clearHeadByEmployeeId(employeeId);
    }

    private Department toDomain(com.khasanshin.organizationservice.entity.Department e) {
        return Department.builder()
                .id(e.getId())
                .code(e.getCode())
                .name(e.getName())
                .facultyId(e.getFaculty())
                .headEmployeeId(e.getHeadEmployee())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private com.khasanshin.organizationservice.entity.Department toEntity(Department d) {
        return com.khasanshin.organizationservice.entity.Department.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .faculty(d.getFacultyId())
                .headEmployee(d.getHeadEmployeeId())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
