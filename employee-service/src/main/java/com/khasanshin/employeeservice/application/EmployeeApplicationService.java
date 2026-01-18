package com.khasanshin.employeeservice.application;

import com.khasanshin.employeeservice.domain.model.Employee;
import com.khasanshin.employeeservice.domain.port.EmployeeRepositoryPort;
import com.khasanshin.employeeservice.domain.port.OrgVerifierPort;
import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import com.khasanshin.employeeservice.mapper.EmployeeMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeApplicationService implements EmployeeUseCase {

    private final EmployeeRepositoryPort employeeRepository;
    private final EmployeeMapper mapper;
    private final OrgVerifierPort orgVerifier;

    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    @Override
    public boolean exists(UUID id) {
        return employeeRepository.existsById(id);
    }

    @Override
    @Transactional
    public EmployeeDto create(CreateEmployeeDto dto) {
        Employee employee = mapper.toDomain(dto);

        if (dto.getDepartmentId() != null) {
            orgVerifier.ensureDepartmentExists(dto.getDepartmentId());
            employee = employee.toBuilder().department(dto.getDepartmentId()).build();
        }

        return mapper.toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeDto update(UUID id, UpdateEmployeeDto dto) {
        Employee employee =
                employeeRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));

        Employee updated = employee.toBuilder().build();

        if (dto.getFirstName() != null) {
            updated = updated.toBuilder().firstName(dto.getFirstName()).build();
        }

        if (dto.getLastName() != null) {
            updated = updated.toBuilder().lastName(dto.getLastName()).build();
        }

        if (dto.getMiddleName() != null) {
            updated = updated.toBuilder().middleName(dto.getMiddleName()).build();
        }

        if (dto.getDepartmentId() != null) {
            orgVerifier.ensureDepartmentExists(dto.getDepartmentId());
            updated = updated.toBuilder().department(dto.getDepartmentId()).build();
        }

        return mapper.toDto(employeeRepository.save(updated));
    }

    @Override
    public EmployeeDto get(UUID id) {
        Employee d =
                employeeRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
        return mapper.toDto(d);
    }

    @Override
    @Transactional
    public EmployeeDto fire(UUID id) {
        Employee employee =
                employeeRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));

        if (employee.getStatus() == Employee.Status.FIRED) {
            return mapper.toDto(employee);
        }

        orgVerifier.clearHeadByEmployee(id);

        Employee fired = employee.toBuilder()
                .department(null)
                .status(Employee.Status.FIRED)
                .build();
        return mapper.toDto(employeeRepository.save(fired));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!employeeRepository.existsById(id)) {
            throw new EntityNotFoundException("employee not found: " + id);
        }
        try {
            employeeRepository.deleteById(id);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("cannot delete employee with references", ex);
        }
    }

    @Override
    public Page<EmployeeDto> findAll(Pageable pageable) {
        Pageable sorted =
                pageable.getSort().isSorted()
                        ? pageable
                        : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        return employeeRepository.findAll(sorted).map(mapper::toDto);
    }

    @Override
    public Map<String, Object> stream(Instant cursor, int size) {
        int limit = Math.max(1, Math.min(size, 50));
        Pageable pageReq = PageRequest.of(0, limit, DEFAULT_SORT);

        Slice<Employee> slice =
                (cursor == null)
                        ? employeeRepository.findAllBy(pageReq)
                        : employeeRepository.findByCreatedAtLessThan(cursor, pageReq);

        List<EmployeeDto> items = slice.getContent().stream().map(mapper::toDto).toList();

        Instant nextCursor = items.isEmpty() ? null : items.getLast().getCreatedAt();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("items", items);
        payload.put("nextCursor", slice.hasNext() ? nextCursor : null);
        payload.put("hasNext", slice.hasNext());
        return payload;
    }

    @Override
    @Transactional
    public EmployeeDto activate(UUID id) {
        Employee e =
                employeeRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
        Employee activated = e.toBuilder().status(Employee.Status.ACTIVE).build();
        return mapper.toDto(employeeRepository.save(activated));
    }
}
