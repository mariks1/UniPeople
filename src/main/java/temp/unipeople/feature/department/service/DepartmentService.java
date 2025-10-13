package temp.unipeople.feature.department.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.department.dto.CreateDepartmentDto;
import temp.unipeople.feature.department.dto.DepartmentDto;
import temp.unipeople.feature.department.dto.UpdateDepartmentDto;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.department.mapper.DepartmentMapper;
import temp.unipeople.feature.department.repository.DepartmentRepository;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.service.EmployeeReader;
import temp.unipeople.feature.faculty.entity.Faculty;

@Service
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final DepartmentMapper mapper;
  private final EntityManager em;
  private final EmployeeReader employeeReader;

  @Transactional(readOnly = true)
  public DepartmentDto get(UUID id) {
    Department d =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));
    return mapper.toDto(d);
  }

  @Transactional
  public DepartmentDto create(CreateDepartmentDto dto) {
    Department e = mapper.toEntity(dto);
    e.setFaculty(em.getReference(Faculty.class, dto.getFacultyId()));
    if (dto.getHeadEmployeeId() != null) {
      e.setHeadEmployee(em.getReference(Employee.class, dto.getHeadEmployeeId()));
    }
    Department saved = departmentRepository.save(e);
    return mapper.toDto(saved);
  }

  @Transactional
  public DepartmentDto update(UUID id, UpdateDepartmentDto dto) {
    Department e =
        departmentRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("department not found: " + id));

    mapper.updateEntity(dto, e);

    if (dto.getFacultyId() != null) {
      e.setFaculty(em.getReference(Faculty.class, dto.getFacultyId()));
    }
    if (dto.getHeadEmployeeId() != null) {
      e.setHeadEmployee(em.getReference(Employee.class, dto.getHeadEmployeeId()));
    }

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

    Employee emp = employeeReader.require(employeeId);

    dep.setHeadEmployee(emp);
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

  @Transactional(readOnly = true)
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
