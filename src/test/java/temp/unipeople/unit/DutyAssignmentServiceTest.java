package temp.unipeople.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.duty.dto.AssignDutyDto;
import temp.unipeople.feature.duty.dto.DutyAssignmentDto;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;
import temp.unipeople.feature.duty.entity.Duty;
import temp.unipeople.feature.duty.mapper.DutyAssignmentMapper;
import temp.unipeople.feature.duty.repository.DutyAssignmentRepository;
import temp.unipeople.feature.duty.repository.DutyRepository;
import temp.unipeople.feature.duty.service.DutyAssignmentService;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
class DutyAssignmentServiceTest {

  @Mock DutyRepository dutyRepository;
  @Mock EmployeeRepository employeeRepo;
  @Mock DutyRepository dutyRepo;
  @Mock DutyAssignmentRepository assignmentRepo;
  @Mock DutyAssignmentMapper mapper;
  @Mock EntityManager em;

  DutyAssignmentService service;

  @BeforeEach
  void setUp() {
    service =
        new DutyAssignmentService(
            dutyRepository, employeeRepo, dutyRepo, assignmentRepo, mapper, em);
  }

  @Test
  void assign_throws_whenDuplicateExists() {
    UUID depId = UUID.randomUUID();
    UUID empId = UUID.randomUUID();
    UUID dutyId = UUID.randomUUID();

    when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(depId, empId, dutyId))
        .thenReturn(true);

    var req = AssignDutyDto.builder().employeeId(empId).dutyId(dutyId).note("n").build();

    assertThrows(IllegalStateException.class, () -> service.assign(depId, req));
    verify(assignmentRepo, never()).saveAndFlush(any());
  }

  @Test
  void assign_ok_savesAndMaps() {
    UUID depId = UUID.randomUUID();
    UUID empId = UUID.randomUUID();
    UUID dutyId = UUID.randomUUID();

    when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(depId, empId, dutyId))
        .thenReturn(false);

    when(em.getReference(Department.class, depId)).thenReturn(new Department());
    when(em.getReference(Employee.class, empId)).thenReturn(new Employee());
    when(em.getReference(Duty.class, dutyId)).thenReturn(new Duty());

    DepartmentDutyAssignment saved = DepartmentDutyAssignment.builder().build();
    when(assignmentRepo.saveAndFlush(any(DepartmentDutyAssignment.class))).thenReturn(saved);
    when(mapper.toDto(saved)).thenReturn(DutyAssignmentDto.builder().build());

    var req = AssignDutyDto.builder().employeeId(empId).dutyId(dutyId).note("note").build();

    var dto = service.assign(depId, req);

    assertNotNull(dto);
    verify(assignmentRepo).saveAndFlush(any(DepartmentDutyAssignment.class));
    verify(mapper).toDto(saved);
  }

  @Test
  void assign_wraps_DataIntegrityViolation() {
    UUID depId = UUID.randomUUID();
    UUID empId = UUID.randomUUID();
    UUID dutyId = UUID.randomUUID();

    when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(depId, empId, dutyId))
        .thenReturn(false);

    when(em.getReference(Department.class, depId)).thenReturn(new Department());
    when(em.getReference(Employee.class, empId)).thenReturn(new Employee());
    when(em.getReference(Duty.class, dutyId)).thenReturn(new Duty());

    when(assignmentRepo.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("dup"));

    var req = AssignDutyDto.builder().employeeId(empId).dutyId(dutyId).note("note").build();

    assertThrows(IllegalStateException.class, () -> service.assign(depId, req));
  }

  @Test
  void list_appliesDefaultSortAndMaps() {
    UUID depId = UUID.randomUUID();
    Pageable in = PageRequest.of(0, 5);

    DepartmentDutyAssignment entity = DepartmentDutyAssignment.builder().build();
    Page<DepartmentDutyAssignment> page = new PageImpl<>(List.of(entity), in, 1);

    when(assignmentRepo.findByDepartmentId(any(), any())).thenReturn(page);
    when(mapper.toDto(entity)).thenReturn(DutyAssignmentDto.builder().build());

    var result = service.list(depId, in);

    assertEquals(1, result.getTotalElements());

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(assignmentRepo).findByDepartmentId(eq(depId), captor.capture());
    Sort.Order order = captor.getValue().getSort().getOrderFor("assigned_at");
    assertNotNull(order);
    assertEquals(Sort.Direction.DESC, order.getDirection());
  }

  @Test
  void unassign_throws_whenNotFound() {
    UUID depId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();

    when(assignmentRepo.findById(assignmentId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.unassign(depId, assignmentId));
  }

  @Test
  void unassign_throws_whenAssignmentInAnotherDepartment() {
    UUID depId = UUID.randomUUID();
    UUID otherDeptId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();

    Department otherDep = mock(Department.class);
    when(otherDep.getId()).thenReturn(otherDeptId);

    DepartmentDutyAssignment a = mock(DepartmentDutyAssignment.class);
    when(a.getDepartment()).thenReturn(otherDep);

    when(assignmentRepo.findById(assignmentId)).thenReturn(Optional.of(a));

    assertThrows(EntityNotFoundException.class, () -> service.unassign(depId, assignmentId));
    verify(assignmentRepo, never()).delete(any());
  }

  @Test
  void unassign_ok_deletes() {
    UUID depId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();

    Department thisDep = mock(Department.class);
    when(thisDep.getId()).thenReturn(depId);

    DepartmentDutyAssignment a = mock(DepartmentDutyAssignment.class);
    when(a.getDepartment()).thenReturn(thisDep);

    when(assignmentRepo.findById(assignmentId)).thenReturn(Optional.of(a));

    service.unassign(depId, assignmentId);

    verify(assignmentRepo).delete(a);
  }
}
