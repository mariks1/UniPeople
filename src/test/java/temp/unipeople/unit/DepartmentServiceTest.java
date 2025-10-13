package temp.unipeople.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import temp.unipeople.feature.department.dto.*;
import temp.unipeople.feature.department.entity.Department;
import temp.unipeople.feature.department.mapper.DepartmentMapper;
import temp.unipeople.feature.department.repository.DepartmentRepository;
import temp.unipeople.feature.department.service.DepartmentService;
import temp.unipeople.feature.employee.entity.Employee;
import temp.unipeople.feature.employee.service.EmployeeReader;
import temp.unipeople.feature.faculty.entity.Faculty;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

  @Mock DepartmentRepository departmentRepository;
  @Mock DepartmentMapper mapper;
  @Mock EntityManager em;
  @Mock EmployeeReader employeeReader;

  DepartmentService service;

  @BeforeEach
  void setUp() {
    service = new DepartmentService(departmentRepository, mapper, em, employeeReader);
  }

  @Test
  void get_ok() {
    UUID id = UUID.randomUUID();
    Department d = new Department();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(d));
    when(mapper.toDto(d)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.get(id));
  }

  @Test
  void get_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
  }

  @Test
  void create_setsFacultyAndOptionalHead_thenSaves() {
    var dto =
        CreateDepartmentDto.builder()
            .name("Dept")
            .facultyId(UUID.randomUUID())
            .headEmployeeId(UUID.randomUUID())
            .build();

    Department entity = new Department();
    Department saved = new Department();

    when(mapper.toEntity(dto)).thenReturn(entity);
    when(em.getReference(eq(Faculty.class), eq(dto.getFacultyId()))).thenReturn(new Faculty());
    when(em.getReference(eq(Employee.class), eq(dto.getHeadEmployeeId())))
        .thenReturn(new Employee());
    when(departmentRepository.save(entity)).thenReturn(saved);
    when(mapper.toDto(saved)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.create(dto));
    verify(em).getReference(Faculty.class, dto.getFacultyId());
    verify(em).getReference(Employee.class, dto.getHeadEmployeeId());
    verify(departmentRepository).save(entity);
  }

  @Test
  void create_withoutHead_setsOnlyFaculty() {
    var dto = CreateDepartmentDto.builder().name("Dept").facultyId(UUID.randomUUID()).build();

    Department entity = new Department();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(em.getReference(eq(Faculty.class), eq(dto.getFacultyId()))).thenReturn(new Faculty());
    when(departmentRepository.save(entity)).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.create(dto));
    verify(em, never()).getReference(eq(Employee.class), any());
  }

  @Test
  void update_ok_updatesEntity_andSetsRefsWhenProvided() {
    UUID id = UUID.randomUUID();
    var dto =
        UpdateDepartmentDto.builder()
            .name("New")
            .facultyId(UUID.randomUUID())
            .headEmployeeId(UUID.randomUUID())
            .build();

    Department e = new Department();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.update(id, dto));
    verify(mapper).updateEntity(dto, e);
    verify(em).getReference(Faculty.class, dto.getFacultyId());
    verify(em).getReference(Employee.class, dto.getHeadEmployeeId());
  }

  @Test
  void update_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.update(UUID.randomUUID(), UpdateDepartmentDto.builder().build()));
  }

  @Test
  void update_doesNotTouchRefsWhenIdsNull() {
    UUID id = UUID.randomUUID();
    var dto = UpdateDepartmentDto.builder().name("Only name").build();

    Department e = new Department();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(DepartmentDto.builder().build());

    service.update(id, dto);

    verify(em, never()).getReference(eq(Faculty.class), any());
    verify(em, never()).getReference(eq(Employee.class), any());
  }

  @Test
  void delete_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.delete(UUID.randomUUID()));
  }

  @Test
  void delete_ok_nullsHeadAndDeletes() {
    UUID id = UUID.randomUUID();
    Department dep = spy(new Department());
    when(departmentRepository.findById(id)).thenReturn(Optional.of(dep));

    service.delete(id);

    verify(dep).setHeadEmployee(null);
    verify(departmentRepository).delete(dep);
  }

  @Test
  void setHead_ok() {
    UUID depId = UUID.randomUUID();
    UUID empId = UUID.randomUUID();

    Department dep = new Department();
    Employee emp = new Employee();

    when(departmentRepository.findById(depId)).thenReturn(Optional.of(dep));
    when(employeeReader.require(empId)).thenReturn(emp);
    when(mapper.toDto(dep)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.setHead(depId, empId));
    assertSame(emp, dep.getHeadEmployee());
  }

  @Test
  void setHead_depNotFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class, () -> service.setHead(UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void setHead_empNotFound() {
    UUID depId = UUID.randomUUID();
    when(departmentRepository.findById(depId)).thenReturn(Optional.of(new Department()));
    when(employeeReader.require(any()))
        .thenThrow(new EntityNotFoundException("employee not found"));

    assertThrows(EntityNotFoundException.class, () -> service.setHead(depId, UUID.randomUUID()));
  }

  @Test
  void removeHead_ok() {
    UUID depId = UUID.randomUUID();
    Department dep = spy(new Department());
    when(departmentRepository.findById(depId)).thenReturn(Optional.of(dep));

    service.removeHead(depId);

    verify(dep).setHeadEmployee(null);
  }

  @Test
  void removeHead_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.removeHead(UUID.randomUUID()));
  }

  @Test
  void findAll_appliesDefaultSortWhenNone() {
    Pageable in = PageRequest.of(0, 20);
    when(departmentRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(departmentRepository).findAll(captor.capture());
    Sort.Order order = captor.getValue().getSort().getOrderFor("name");
    assertNotNull(order);
    assertEquals(Sort.Direction.ASC, order.getDirection());
  }

  @Test
  void findAll_keepsProvidedSort() {
    Pageable in = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "created_at"));
    when(departmentRepository.findAll(in)).thenReturn(Page.empty());

    service.findAll(in);

    verify(departmentRepository).findAll(in);
  }
}
