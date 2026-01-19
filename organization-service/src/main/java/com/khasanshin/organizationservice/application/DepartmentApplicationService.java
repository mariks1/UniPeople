package com.khasanshin.organizationservice.application;

import com.khasanshin.organizationservice.domain.model.Department;
import com.khasanshin.organizationservice.domain.port.DepartmentRepositoryPort;
import com.khasanshin.organizationservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.organizationservice.domain.port.FacultyRepositoryPort;
import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.mapper.DepartmentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentApplicationService implements DepartmentUseCase {

    private final DepartmentRepositoryPort departmentRepository;
    private final DepartmentMapper mapper;
    private final FacultyRepositoryPort facultyRepository;
    private final EmployeeVerifierPort employeeVerifier;

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");
    private static final Set<String> ALLOWED_SORT = Set.of("id", "name", "code", "createdAt", "updatedAt");

    private Sort sanitizeSort(Sort incoming) {
        List<Sort.Order> safe = new ArrayList<>();
        for (Sort.Order o : incoming) {
            if (ALLOWED_SORT.contains(o.getProperty())) {
                safe.add(o);
            }
        }
        return safe.isEmpty() ? DEFAULT_SORT : Sort.by(safe);
    }

    @Override
    public DepartmentDto get(UUID id) {
        Department d = departmentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));
        return mapper.toDto(d);
    }

    @Override
    @Transactional
    public DepartmentDto create(CreateDepartmentDto dto) {

        if (dto.getFacultyId() == null || !facultyRepository.existsById(dto.getFacultyId())) {
            throw new EntityNotFoundException("faculty " + dto.getFacultyId());
        }

        if (dto.getHeadEmployeeId() != null) {
            employeeVerifier.ensureEmployeeExists(dto.getHeadEmployeeId());
        }

        Department e = mapper.toDomain(dto);
        return mapper.toDto(departmentRepository.save(e));
    }

    @Override
    @Transactional
    public DepartmentDto update(UUID id, UpdateDepartmentDto dto) {
        Department e = departmentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));

        Department updated = mapper.updateDomain(dto, e);

        if (dto.getFacultyId() != null && !facultyRepository.existsById(dto.getFacultyId())) {
            throw new EntityNotFoundException("faculty " + dto.getFacultyId());
        }
        if (dto.getHeadEmployeeId() != null) {
            employeeVerifier.ensureEmployeeExists(dto.getHeadEmployeeId());
        }

        Department saved = departmentRepository.save(updated);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Department dep = departmentRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));

        Department cleared = dep.toBuilder().headEmployeeId(null).build();

        departmentRepository.delete(cleared);
    }

    @Override
    @Transactional
    public DepartmentDto setHead(UUID deptId, UUID employeeId) {
        Department dep = departmentRepository
                .findById(deptId)
                .orElseThrow(() -> new EntityNotFoundException("department not found: " + deptId));

        employeeVerifier.ensureEmployeeExists(employeeId);
        Department updated = dep.toBuilder().headEmployeeId(employeeId).build();
        return mapper.toDto(departmentRepository.save(updated));
    }

    @Override
    public Page<DepartmentDto> findAll(Pageable pageable) {
        Pageable sorted =
                pageable.getSort().isSorted()
                        ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sanitizeSort(pageable.getSort()))
                        : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        return departmentRepository.findAll(sorted).map(mapper::toDto);
    }

    @Override
    @Transactional
    public void removeHead(UUID deptId) {
        Department dep = departmentRepository
                .findById(deptId)
                .orElseThrow(() -> new EntityNotFoundException("department not found: " + deptId));
        Department cleared = dep.toBuilder().headEmployeeId(null).build();
        departmentRepository.save(cleared);
    }

    @Override
    public boolean exists(UUID id) {
        return departmentRepository.existsById(id);
    }

    @Override
    @Transactional
    public int clearHeadByEmployee(UUID employeeId) {
        return departmentRepository.clearHeadByEmployeeId(employeeId);
    }
}
