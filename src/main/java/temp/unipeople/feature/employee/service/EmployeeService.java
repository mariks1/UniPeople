package temp.unipeople.feature.employee.service;

import jakarta.persistence.EntityManager;
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
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.department.repository.DepartmentRepository;
import temp.unipeople.feature.employee.dto.CreateEmployeeDto;
import temp.unipeople.feature.employee.dto.EmployeeDto;
import temp.unipeople.feature.employee.dto.UpdateEmployeeDto;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.mapper.EmployeeMapper;
import temp.unipeople.feature.employee.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final EmployeeMapper mapper;
  private final EntityManager entityManager;

  @Transactional
  public EmployeeDto create(CreateEmployeeDto dto) {
    Employee employee = mapper.toEntity(dto);
    if (dto.getDepartmentId() != null) {
      employee.setDepartment(entityManager.find(Department.class, dto.getDepartmentId()));
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
      employee.setDepartment(entityManager.find(Department.class, dto.getDepartmentId()));
    }

    return mapper.toDto(employee);
  }

  @Transactional(readOnly = true)
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

    departmentRepository.clearHeadByEmployeeId(id);

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

  @Transactional(readOnly = true)
  public Page<EmployeeDto> findAll(Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    return employeeRepository.findAll(sorted).map(mapper::toDto);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> stream(Instant cursor, int size) {
    int limit = Math.max(1, Math.min(size, 50));
    Pageable pageReq = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

    Slice<Employee> slice =
        (cursor == null)
            ? employeeRepository.findAllByOrderByCreatedAtDesc(pageReq)
            : employeeRepository.findByCreatedAtLessThanOrderByCreatedAtDesc(cursor, pageReq);

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
