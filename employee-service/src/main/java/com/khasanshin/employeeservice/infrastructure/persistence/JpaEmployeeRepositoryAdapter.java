package com.khasanshin.employeeservice.infrastructure.persistence;

import com.khasanshin.employeeservice.domain.model.Employee;
import com.khasanshin.employeeservice.domain.port.EmployeeRepositoryPort;
import com.khasanshin.employeeservice.repository.EmployeeRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaEmployeeRepositoryAdapter implements EmployeeRepositoryPort {

    private final EmployeeRepository repository;

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public Employee save(Employee employee) {
        return toDomain(repository.save(toEntity(employee)));
    }

    @Override
    public Optional<Employee> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public Page<Employee> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toDomain);
    }

    @Override
    public Slice<Employee> findAllBy(Pageable pageable) {
        return repository.findAllBy(pageable).map(this::toDomain);
    }

    @Override
    public Slice<Employee> findByCreatedAtLessThan(Instant cursor, Pageable pageable) {
        return repository.findByCreatedAtLessThan(cursor, pageable).map(this::toDomain);
    }

    private Employee toDomain(com.khasanshin.employeeservice.entity.Employee e) {
        return Employee.builder()
                .id(e.getId())
                .version(e.getVersion())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .middleName(e.getMiddleName())
                .workEmail(e.getWorkEmail())
                .phone(e.getPhone())
                .status(Employee.Status.valueOf(e.getStatus().name()))
                .department(e.getDepartment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private com.khasanshin.employeeservice.entity.Employee toEntity(Employee e) {
        return com.khasanshin.employeeservice.entity.Employee.builder()
                .id(e.getId())
                .version(e.getVersion())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .middleName(e.getMiddleName())
                .workEmail(e.getWorkEmail())
                .phone(e.getPhone())
                .status(com.khasanshin.employeeservice.entity.Employee.Status.valueOf(e.getStatus().name()))
                .department(e.getDepartment())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
