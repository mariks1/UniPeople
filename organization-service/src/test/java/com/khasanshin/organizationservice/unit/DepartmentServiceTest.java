package com.khasanshin.organizationservice.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.entity.Department;
import com.khasanshin.organizationservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.organizationservice.feign.EmployeeClient;
import com.khasanshin.organizationservice.feign.EmployeeVerifier;
import com.khasanshin.organizationservice.mapper.DepartmentMapper;
import com.khasanshin.organizationservice.repository.DepartmentRepository;
import com.khasanshin.organizationservice.repository.FacultyRepository;
import com.khasanshin.organizationservice.service.DepartmentService;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

  @Mock DepartmentRepository departmentRepository;
  @Mock
  FacultyRepository facultyRepository;
  @Mock DepartmentMapper mapper;
  @Mock
  EmployeeVerifier employeeVerifier;

  DepartmentService service;

  @BeforeEach
  void setUp() {
    service = new DepartmentService(departmentRepository, mapper, facultyRepository, employeeVerifier);
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
  void create_ok_validatesFacultyAndHead_thenSaves() {
    var dto = CreateDepartmentDto.builder()
            .name("Dept").code("D1")
            .facultyId(UUID.randomUUID())
            .headEmployeeId(UUID.randomUUID())
            .build();

    when(facultyRepository.existsById(dto.getFacultyId())).thenReturn(true);
    doNothing().when(employeeVerifier).ensureEmployeeExists(dto.getHeadEmployeeId());

    Department entity = new Department();
    Department saved = new Department();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(departmentRepository.save(entity)).thenReturn(saved);
    when(mapper.toDto(saved)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.create(dto));

    verify(facultyRepository).existsById(dto.getFacultyId());
    verify(employeeVerifier).ensureEmployeeExists(dto.getHeadEmployeeId());
    verify(departmentRepository).save(entity);
  }

  @Test
  void create_rejectsMissingFaculty() {
    var dto = CreateDepartmentDto.builder()
            .name("Dept").facultyId(UUID.randomUUID()).build();

    when(facultyRepository.existsById(dto.getFacultyId())).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> service.create(dto));
    verifyNoInteractions(employeeVerifier);
    verify(departmentRepository, never()).save(any());
  }

  @Test
  void create_headNotFound_mapsTo404() {
    var dto = CreateDepartmentDto.builder()
            .name("Dept").facultyId(UUID.randomUUID())
            .headEmployeeId(UUID.randomUUID()).build();

    when(facultyRepository.existsById(dto.getFacultyId())).thenReturn(true);
    doThrow(new EntityNotFoundException("not found"))
            .when(employeeVerifier).ensureEmployeeExists(dto.getHeadEmployeeId());

    assertThrows(EntityNotFoundException.class, () -> service.create(dto));
    verify(departmentRepository, never()).save(any());
  }

  @Test
  void update_ok_updatesEntity_andValidates() {
    UUID id = UUID.randomUUID();
    var dto = UpdateDepartmentDto.builder()
            .name("NewName").facultyId(UUID.randomUUID())
            .headEmployeeId(UUID.randomUUID()).build();

    Department e = new Department();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(e));
    when(facultyRepository.existsById(dto.getFacultyId())).thenReturn(true);
    doNothing().when(employeeVerifier).ensureEmployeeExists(dto.getHeadEmployeeId());
    when(mapper.toDto(e)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.update(id, dto));

    verify(mapper, times(2)).updateEntity(dto, e);
  }

  @Test
  void setHead_remoteUnavailable_bubblesUp() {
    UUID depId = UUID.randomUUID();
    UUID empId = UUID.randomUUID();

    when(departmentRepository.findById(depId)).thenReturn(Optional.of(new Department()));
    doThrow(new RemoteServiceUnavailableException("employee-service unavailable", null))
            .when(employeeVerifier).ensureEmployeeExists(eq(empId)); // match the actual arg
    assertThrows(RemoteServiceUnavailableException.class, () -> service.setHead(depId, empId));
    verify(employeeVerifier).ensureEmployeeExists(empId); // optional, proves invocation
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
    when(departmentRepository.findById(depId)).thenReturn(Optional.of(dep));
    doNothing().when(employeeVerifier).ensureEmployeeExists(empId);
    when(mapper.toDto(dep)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.setHead(depId, empId));
    assertEquals(empId, dep.getHeadEmployee());
  }

  @Test
  void findAll_appliesDefaultSort() {
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
  void removeHead_ok_nullsHead() {
    UUID id = UUID.randomUUID();
    Department dep = new Department();
    dep.setHeadEmployee(UUID.randomUUID());

    when(departmentRepository.findById(id)).thenReturn(Optional.of(dep));

    service.removeHead(id);

    assertNull(dep.getHeadEmployee());
  }

  @Test
  void setHead_departmentNotFound_throws404() {
    UUID depId = UUID.randomUUID();
    when(departmentRepository.findById(depId)).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.setHead(depId, UUID.randomUUID()));
    verifyNoInteractions(employeeVerifier);
  }

  @Test
  void update_rejectsMissingFaculty() {
    UUID id = UUID.randomUUID();
    UpdateDepartmentDto dto = UpdateDepartmentDto.builder()
            .facultyId(UUID.randomUUID())
            .build();

    when(departmentRepository.findById(id)).thenReturn(Optional.of(new Department()));
    when(facultyRepository.existsById(dto.getFacultyId())).thenReturn(false);

    assertThrows(EntityNotFoundException.class, () -> service.update(id, dto));
    verifyNoInteractions(employeeVerifier);
  }

  @Test
  void update_headNotFound_mapsTo404() {
    UUID id = UUID.randomUUID();
    UpdateDepartmentDto dto = UpdateDepartmentDto.builder()
            .headEmployeeId(UUID.randomUUID())
            .build();

    Department dep = new Department();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(dep));
    doThrow(new EntityNotFoundException("employeeId not found"))
            .when(employeeVerifier).ensureEmployeeExists(dto.getHeadEmployeeId());


    assertThrows(EntityNotFoundException.class, () -> service.update(id, dto));
  }

  @Test
  void findAll_respectsIncomingAllowedSort() {
    Pageable in = PageRequest.of(1, 15, Sort.by(Sort.Order.desc("code"), Sort.Order.asc("name")));
    when(departmentRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(departmentRepository).findAll(captor.capture());
    Sort sort = captor.getValue().getSort();

    assertNotNull(sort.getOrderFor("code"));
    assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("code")).getDirection());
    assertNotNull(sort.getOrderFor("name"));
    assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("name")).getDirection());
    assertEquals(2, sort.stream().count());
  }

  @Test
  void findAll_filtersUnknownSort_toDefault() {
    Pageable in = PageRequest.of(0, 10, Sort.by("unknown"));
    when(departmentRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(departmentRepository).findAll(captor.capture());
    Sort sort = captor.getValue().getSort();

    assertNotNull(sort.getOrderFor("name"));
    assertEquals(Sort.Direction.ASC, Objects.requireNonNull(sort.getOrderFor("name")).getDirection());
    assertNull(sort.getOrderFor("unknown"));
    assertEquals(1, sort.stream().count());
  }

  @Test
  void clearHeadByEmployee_delegatesToRepository() {
    UUID empId = UUID.randomUUID();
    when(departmentRepository.clearHeadByEmployeeId(empId)).thenReturn(5);

    int updated = service.clearHeadByEmployee(empId);

    assertEquals(5, updated);
    verify(departmentRepository).clearHeadByEmployeeId(empId);
  }

  @Test
  void exists_ok() {
    UUID id = UUID.randomUUID();
    when(departmentRepository.existsById(id)).thenReturn(true);

    assertTrue(service.exists(id));
    verify(departmentRepository).existsById(id);
  }


}
