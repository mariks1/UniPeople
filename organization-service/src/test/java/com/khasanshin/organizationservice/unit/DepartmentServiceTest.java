package com.khasanshin.organizationservice.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.khasanshin.organizationservice.application.DepartmentApplicationService;
import com.khasanshin.organizationservice.domain.model.Department;
import com.khasanshin.organizationservice.domain.port.DepartmentRepositoryPort;
import com.khasanshin.organizationservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.organizationservice.domain.port.FacultyRepositoryPort;
import com.khasanshin.organizationservice.dto.CreateDepartmentDto;
import com.khasanshin.organizationservice.dto.DepartmentDto;
import com.khasanshin.organizationservice.dto.UpdateDepartmentDto;
import com.khasanshin.organizationservice.mapper.DepartmentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

  @Mock DepartmentRepositoryPort departmentRepository;
  @Mock FacultyRepositoryPort facultyRepository;
  @Mock DepartmentMapper mapper;
  @Mock EmployeeVerifierPort employeeVerifier;

  DepartmentApplicationService service;

  @BeforeEach
  void setUp() {
    service = new DepartmentApplicationService(departmentRepository, mapper, facultyRepository, employeeVerifier);
  }

  @Test
  void get_ok() {
    UUID id = UUID.randomUUID();
    Department e = Department.builder().id(id).name("X").build();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(DepartmentDto.builder().build());

    assertNotNull(service.get(id));
  }

  @Test
  void get_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
  }

  @Test
  void create_validatesFaculty_andHead() {
    UUID faculty = UUID.randomUUID();
    UUID head = UUID.randomUUID();
    CreateDepartmentDto dto = CreateDepartmentDto.builder()
            .name("N").code("C1").facultyId(faculty).headEmployeeId(head).build();

    when(facultyRepository.existsById(faculty)).thenReturn(true);
    Department toSave = Department.builder().name("N").code("C1").facultyId(faculty).headEmployeeId(head).build();
    Department saved = toSave.toBuilder().id(UUID.randomUUID()).build();
    when(mapper.toDomain(dto)).thenReturn(toSave);
    when(departmentRepository.save(toSave)).thenReturn(saved);
    when(mapper.toDto(saved)).thenReturn(DepartmentDto.builder().id(saved.getId()).build());

    DepartmentDto out = service.create(dto);

    assertNotNull(out);
    verify(employeeVerifier).ensureEmployeeExists(head);
  }

  @Test
  void update_notFound() {
    when(departmentRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.update(UUID.randomUUID(), UpdateDepartmentDto.builder().build()));
  }

  @Test
  void delete_clearsHead() {
    UUID id = UUID.randomUUID();
    Department dep = Department.builder().id(id).headEmployeeId(UUID.randomUUID()).build();
    when(departmentRepository.findById(id)).thenReturn(Optional.of(dep));

    service.delete(id);

    verify(departmentRepository).delete(dep.toBuilder().headEmployeeId(null).build());
  }

  @Test
  void findAll_sanitizesSort() {
    Pageable in = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("unknown")));
    when(departmentRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(departmentRepository).findAll(captor.capture());
    Sort sort = captor.getValue().getSort();
    assertEquals(Sort.by(Sort.Order.asc("name")), sort);
  }
}
