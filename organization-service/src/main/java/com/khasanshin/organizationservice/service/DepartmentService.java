package com.khasanshin.organizationservice.service;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.entity.Department;
import com.khasanshin.organizationservice.feign.EmployeeClient;
import com.khasanshin.organizationservice.mapper.DepartmentMapper;
import com.khasanshin.organizationservice.repository.DepartmentRepository;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import jakarta.persistence.EntityNotFoundException;
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
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final DepartmentMapper mapper;
  private final FacultyRepository facultyRepository;
  private final EmployeeClient employeeClient;


  public DepartmentDto get(UUID id) {
    Department d =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));
    return mapper.toDto(d);
  }

  @Transactional
  public DepartmentDto create(CreateDepartmentDto dto) {

    if (dto.getFacultyId() == null || !facultyRepository.existsById(dto.getFacultyId())) {
      throw new EntityNotFoundException("faculty " + dto.getFacultyId());
    }

    if (dto.getHeadEmployeeId() != null) {
      employeeClient.exists(dto.getHeadEmployeeId());
    }

    var e = mapper.toEntity(dto);
    return mapper.toDto(departmentRepository.save(e));

  }

  @Transactional
  public DepartmentDto update(UUID id, UpdateDepartmentDto dto) {
    Department e =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));

    mapper.updateEntity(dto, e);

    if (dto.getFacultyId() != null && !facultyRepository.existsById(dto.getFacultyId())) {
      throw new EntityNotFoundException("faculty " + dto.getFacultyId());
    }
    if (dto.getHeadEmployeeId() != null) {
      employeeClient.exists(dto.getHeadEmployeeId());
    }

    mapper.updateEntity(dto, e);
    return mapper.toDto(e);
  }

  @Transactional
  public void delete(UUID id) {
    Department dep =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));

    dep.setHeadEmployee(null);

    departmentRepository.delete(dep);
  }

  @Transactional
  public DepartmentDto setHead(UUID deptId, UUID employeeId) {
    Department dep =
        departmentRepository
            .findById(deptId)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + deptId));

    employeeClient.exists(employeeId);
    dep.setHeadEmployee(employeeId);
    return mapper.toDto(dep);
  }

  @Transactional
  public void removeHead(UUID deptId) {
    Department dep =
        departmentRepository
            .findById(deptId)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + deptId));
    dep.setHeadEmployee(null);
  }

  public Page<DepartmentDto> findAll(Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "name"));
    return departmentRepository.findAll(sorted).map(mapper::toDto);
  }
}
