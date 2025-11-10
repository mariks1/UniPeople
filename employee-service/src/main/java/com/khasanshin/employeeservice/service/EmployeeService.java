package com.khasanshin.employeeservice.service;

import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import com.khasanshin.employeeservice.entity.Employee;
import com.khasanshin.employeeservice.feign.OrgVerifier;
import com.khasanshin.employeeservice.mapper.EmployeeMapper;
import com.khasanshin.employeeservice.repository.EmployeeRepository;
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
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final EmployeeMapper mapper;
  private final OrgVerifier orgVerifier;

  private static final Sort DEFAULT_SORT =
      Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

  public boolean exists(UUID id) {
    return employeeRepository.existsById(id);
  }
  @Transactional
  public EmployeeDto create(CreateEmployeeDto dto) {
    Employee employee = mapper.toEntity(dto);

    if (dto.getDepartmentId() != null) {
      orgVerifier.ensureDepartmentExists(dto.getDepartmentId());
      employee.setDepartment(dto.getDepartmentId());
    }

    return mapper.toDto(employeeRepository.save(employee));
  }

  @Transactional
  public EmployeeDto update(UUID id, UpdateEmployeeDto dto) {
    Employee employee =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));

    if (dto.getFirstName() != null) {
      employee.setFirstName(dto.getFirstName());
    }

    if (dto.getLastName() != null) {
      employee.setLastName(dto.getLastName());
    }

    if (dto.getMiddleName() != null) {
      employee.setMiddleName(dto.getMiddleName());
    }

    if (dto.getDepartmentId() != null) {
      orgVerifier.ensureDepartmentExists(dto.getDepartmentId());
      employee.setDepartment(dto.getDepartmentId());
    }

    return mapper.toDto(employee);
  }

  public EmployeeDto get(UUID id) {
    Employee d =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
    return mapper.toDto(d);
  }

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

    employee.setDepartment(null);
    employee.setStatus(Employee.Status.FIRED);
    return mapper.toDto(employee);
  }

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

  public Page<EmployeeDto> findAll(Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
    return employeeRepository.findAll(sorted).map(mapper::toDto);
  }

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

  @Transactional
  public EmployeeDto activate(UUID id) {
    Employee e =
        employeeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employee not found: " + id));
    e.setStatus(Employee.Status.ACTIVE);
    return mapper.toDto(e);
  }

}
