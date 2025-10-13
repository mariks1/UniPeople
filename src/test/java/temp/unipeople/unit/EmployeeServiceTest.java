package temp.unipeople.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.department.repository.DepartmentRepository;
import temp.unipeople.feature.employee.dto.*;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.mapper.EmployeeMapper;
import temp.unipeople.feature.employee.repository.EmployeeRepository;
import temp.unipeople.feature.employee.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

  @Mock EmployeeRepository employeeRepo;
  @Mock DepartmentRepository departmentRepo;
  @Mock EmployeeMapper mapper;
  @Mock EntityManager em;

  EmployeeService service;

  @BeforeEach
  void setUp() {
    service = new EmployeeService(employeeRepo, departmentRepo, mapper, em);
  }

  @Test
  void create_ok_withDept() {
    UUID depId = UUID.randomUUID();
    var dto = CreateEmployeeDto.builder().firstName("A").departmentId(depId).build();
    Employee e = new Employee();
    when(mapper.toEntity(dto)).thenReturn(e);
    when(em.find(Department.class, depId)).thenReturn(new Department());
    when(employeeRepo.save(e)).thenReturn(e);
    when(mapper.toDto(e)).thenReturn(EmployeeDto.builder().build());

    assertNotNull(service.create(dto));
    verify(em).find(Department.class, depId);
  }

  @Test
  void update_notFound() {
    when(employeeRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.update(UUID.randomUUID(), UpdateEmployeeDto.builder().build()));
  }

  @Test
  void update_ok_setsFieldsAndDept() {
    UUID id = UUID.randomUUID();
    UUID depId = UUID.randomUUID();
    Employee e = new Employee();
    when(employeeRepo.findById(id)).thenReturn(Optional.of(e));
    when(em.find(Department.class, depId)).thenReturn(new Department());
    when(mapper.toDto(e)).thenReturn(EmployeeDto.builder().build());

    var dto =
        UpdateEmployeeDto.builder()
            .firstName("F")
            .lastName("L")
            .middleName("M")
            .departmentId(depId)
            .build();

    assertNotNull(service.update(id, dto));
    assertEquals("F", e.getFirstName());
    assertEquals("L", e.getLastName());
    assertEquals("M", e.getMiddleName());
  }

  @Test
  void get_ok_and_notFound() {
    UUID id = UUID.randomUUID();
    Employee e = new Employee();
    when(employeeRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmployeeDto.builder().build());
    assertNotNull(service.get(id));

    when(employeeRepo.findById(id)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(id));
  }

  @Test
  void fire_ok_and_alreadyFired() {
    UUID id = UUID.randomUUID();
    Employee fired = new Employee();
    fired.setStatus(Employee.Status.FIRED);
    when(employeeRepo.findById(id)).thenReturn(Optional.of(fired));
    when(mapper.toDto(fired)).thenReturn(EmployeeDto.builder().build());
    assertNotNull(service.fire(id));

    Employee active = new Employee();
    active.setStatus(Employee.Status.ACTIVE);
    when(employeeRepo.findById(id)).thenReturn(Optional.of(active));
    when(mapper.toDto(active)).thenReturn(EmployeeDto.builder().build());
    assertNotNull(service.fire(id));
    verify(departmentRepo).clearHeadByEmployeeId(id);
    assertNull(active.getDepartment());
    assertEquals(Employee.Status.FIRED, active.getStatus());
  }

  @Test
  void delete_notFound_and_ok_and_wrapsDataIntegrity() {
    UUID id = UUID.randomUUID();
    when(employeeRepo.existsById(id)).thenReturn(false);
    assertThrows(EntityNotFoundException.class, () -> service.delete(id));

    when(employeeRepo.existsById(id)).thenReturn(true);
    doThrow(new DataIntegrityViolationException("ref")).when(employeeRepo).deleteById(id);
    assertThrows(IllegalStateException.class, () -> service.delete(id));

    doNothing().when(employeeRepo).deleteById(id);
    assertDoesNotThrow(() -> service.delete(id));
  }

  @Test
  void findAll_appliesDefaultSort() {
    Pageable in = PageRequest.of(0, 10);
    when(employeeRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(employeeRepo).findAll(captor.capture());
    Pageable passed = captor.getValue();

    Sort.Order order = passed.getSort().getOrderFor("createdAt");
    assertNotNull(order);
    assertTrue(order.isDescending());
  }

  @Test
  void stream_handlesCursorAndSizeBounds() {
    Slice<Employee> s1 = new SliceImpl<>(List.of(new Employee()));
    when(employeeRepo.findAllByOrderByCreatedAtDesc(any())).thenReturn(s1);
    when(mapper.toDto(any(Employee.class)))
        .thenAnswer(inv -> EmployeeDto.builder().createdAt(Instant.now()).build());

    var res1 = service.stream(null, -5);
    assertEquals((boolean) res1.get("hasNext"), s1.hasNext());
    assertTrue(res1.containsKey("nextCursor"));

    Slice<Employee> s2 = new SliceImpl<>(Collections.emptyList());
    when(employeeRepo.findByCreatedAtLessThanOrderByCreatedAtDesc(any(), any())).thenReturn(s2);

    var res2 = service.stream(Instant.now(), 5000);
    assertEquals(false, res2.get("hasNext"));
    assertNull(res2.get("nextCursor"));
  }

  @Test
  void activate_ok_and_notFound() {
    UUID id = UUID.randomUUID();
    Employee e = new Employee();
    when(employeeRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmployeeDto.builder().build());
    assertNotNull(service.activate(id));
    assertEquals(Employee.Status.ACTIVE, e.getStatus());

    when(employeeRepo.findById(id)).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.activate(id));
  }
}
