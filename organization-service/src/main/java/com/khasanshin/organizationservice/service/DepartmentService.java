package com.khasanshin.organizationservice.service;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.entity.Department;
import com.khasanshin.organizationservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.organizationservice.feign.EmployeeClient;
import com.khasanshin.organizationservice.mapper.DepartmentMapper;
import com.khasanshin.organizationservice.repository.DepartmentRepository;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final DepartmentMapper mapper;
  private final FacultyRepository facultyRepository;
  private final EmployeeClient employeeClient;

  private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.ASC, "name");
  private static final Set<String> ALLOWED_SORT =
          Set.of("id", "name", "code", "createdAt", "updatedAt");

  private Sort sanitizeSort(Sort incoming) {
    List<Sort.Order> safe = new ArrayList<>();
    for (Sort.Order o : incoming) {
      if (ALLOWED_SORT.contains(o.getProperty())) {
        safe.add(o);
      }
    }
    return safe.isEmpty() ? DEFAULT_SORT : Sort.by(safe);
  }

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
      ensureEmployeeExists(dto.getHeadEmployeeId());
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
      ensureEmployeeExists(dto.getHeadEmployeeId());
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

    ensureEmployeeExists(employeeId);
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
    Sort sort = sanitizeSort(pageable.getSort());
    Pageable p = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    return departmentRepository.findAll(p).map(mapper::toDto);
  }

  @CircuitBreaker(name = "employeeClient", fallbackMethod = "employeeExistsUnavailable")
  void ensureEmployeeExists(UUID employeeId) {
    try {
      ResponseEntity<Void> resp = employeeClient.exists(employeeId);
      if (resp == null || !resp.getStatusCode().is2xxSuccessful()) {
        throw new EntityNotFoundException("employeeId not found: " + employeeId);
      }
    } catch (FeignException.NotFound e) {
      throw new EntityNotFoundException("employeeId not found: " + employeeId);
    }
  }

  void employeeExistsUnavailable(UUID employeeId, Throwable cause) {
    throw new RemoteServiceUnavailableException("employee-service unavailable", cause);
  }

  @Transactional
  public int clearHeadByEmployee(UUID employeeId) {
    return departmentRepository.clearHeadByEmployeeId(employeeId);
  }

  public boolean exists(UUID id) {
    return departmentRepository.existsById(id);
  }

}
