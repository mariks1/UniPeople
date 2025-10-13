package temp.unipeople.feature.duty.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.duty.dto.AssignDutyDto;
import temp.unipeople.feature.duty.dto.DutyAssignmentDto;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;
import temp.unipeople.feature.duty.entity.Duty;
import temp.unipeople.feature.duty.mapper.DutyAssignmentMapper;
import temp.unipeople.feature.duty.repository.DutyAssignmentRepository;
import temp.unipeople.feature.employee.entity.Employee;

@Service
@RequiredArgsConstructor
public class DutyAssignmentService {

  private final DutyAssignmentRepository assignmentRepo;
  private final DutyAssignmentMapper mapper;
  private final EntityManager em;

  @Transactional
  public DutyAssignmentDto assign(UUID departmentId, AssignDutyDto req) {
    if (assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(
        departmentId, req.getEmployeeId(), req.getDutyId())) {
      throw new IllegalStateException("duty already assigned to employee in this department");
    }

    Department dep = em.getReference(Department.class, departmentId);
    Employee emp = em.getReference(Employee.class, req.getEmployeeId());
    Duty duty = em.getReference(Duty.class, req.getDutyId());

    DepartmentDutyAssignment a =
        DepartmentDutyAssignment.builder()
            .department(dep)
            .employee(emp)
            .duty(duty)
            .note(req.getNote())
            .assignedBy(
                req.getAssignedBy() != null
                    ? em.getReference(Employee.class, req.getAssignedBy())
                    : null)
            .build();

    try {
      a = assignmentRepo.saveAndFlush(a);
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException("invalid or duplicate duty assignment", ex);
    }
    return mapper.toDto(a);
  }

  @Transactional(readOnly = true)
  public Page<DutyAssignmentDto> list(UUID departmentId, Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "assignedAt"));
    return assignmentRepo.findByDepartmentId(departmentId, sorted).map(mapper::toDto);
  }

  @Transactional
  public void unassign(UUID departmentId, UUID assignmentId) {
    var a =
        assignmentRepo
            .findById(assignmentId)
            .orElseThrow(
                () -> new EntityNotFoundException("assignment not found: " + assignmentId));
    if (!a.getDepartment().getId().equals(departmentId)) {
      throw new EntityNotFoundException("assignment not in department");
    }
    assignmentRepo.delete(a);
  }
}
