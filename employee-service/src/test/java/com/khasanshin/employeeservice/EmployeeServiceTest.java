package com.khasanshin.employeeservice;

import com.khasanshin.employeeservice.application.EmployeeApplicationService;
import com.khasanshin.employeeservice.domain.model.Employee;
import com.khasanshin.employeeservice.domain.port.EmployeeRepositoryPort;
import com.khasanshin.employeeservice.domain.port.OrgVerifierPort;
import com.khasanshin.employeeservice.dto.CreateEmployeeDto;
import com.khasanshin.employeeservice.dto.EmployeeDto;
import com.khasanshin.employeeservice.dto.UpdateEmployeeDto;
import com.khasanshin.employeeservice.mapper.EmployeeMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock EmployeeRepositoryPort employeeRepository;
    @Mock EmployeeMapper mapper;
    @Mock OrgVerifierPort orgVerifier;

    EmployeeApplicationService service;

    @BeforeEach
    void setUp() {
        service = new EmployeeApplicationService(employeeRepository, mapper, orgVerifier);
    }

    @Test
    void exists_ok() {
        UUID id = UUID.randomUUID();
        when(employeeRepository.existsById(id)).thenReturn(true);

        assertTrue(service.exists(id));
        verify(employeeRepository).existsById(id);
    }

    @Test
    void create_validatesDepartment_thenSaves() {
        UUID dep = UUID.randomUUID();
        CreateEmployeeDto dto = CreateEmployeeDto.builder().firstName("A").lastName("B").departmentId(dep).build();

        Employee toSave = Employee.builder().firstName("A").lastName("B").department(dep).status(Employee.Status.ACTIVE).build();
        Employee saved = toSave.toBuilder().id(UUID.randomUUID()).build();

        when(mapper.toDomain(dto)).thenReturn(toSave);
        when(employeeRepository.save(toSave)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(EmployeeDto.builder().id(saved.getId()).departmentId(dep).build());

        EmployeeDto out = service.create(dto);

        assertEquals(dep, out.getDepartmentId());
        verify(orgVerifier).ensureDepartmentExists(dep);
        verify(employeeRepository).save(toSave);
    }

    @Test
    void create_duplicateThrowsIllegalState() {
        CreateEmployeeDto dto = CreateEmployeeDto.builder().firstName("A").lastName("B").build();
        Employee toSave = Employee.builder().firstName("A").lastName("B").status(Employee.Status.ACTIVE).build();
        when(mapper.toDomain(dto)).thenReturn(toSave);
        when(employeeRepository.save(toSave)).thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(DataIntegrityViolationException.class, () -> service.create(dto));
    }

    @Test
    void update_notFound_throws404() {
        when(employeeRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.update(UUID.randomUUID(), UpdateEmployeeDto.builder().build()));
    }

    @Test
    void fire_transitionsToFired_andClearsHead() {
        UUID id = UUID.randomUUID();
        Employee current = Employee.builder()
                .id(id).firstName("A").lastName("B").status(Employee.Status.ACTIVE).department(UUID.randomUUID()).build();
        when(employeeRepository.findById(id)).thenReturn(Optional.of(current));
        Employee fired = current.toBuilder().status(Employee.Status.FIRED).department(null).build();
        when(employeeRepository.save(any())).thenReturn(fired);
        when(mapper.toDto(fired)).thenReturn(EmployeeDto.builder().id(id).status(Employee.Status.FIRED).build());

        EmployeeDto out = service.fire(id);

        assertEquals(Employee.Status.FIRED, out.getStatus());
        verify(orgVerifier).clearHeadByEmployee(id);
    }

    @Test
    void delete_notFound_throws404() {
        when(employeeRepository.existsById(any())).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> service.delete(UUID.randomUUID()));
        verify(employeeRepository, never()).deleteById(any());
    }

    @Test
    void findAll_appliesDefaultSort_whenUnsorted() {
        Pageable in = PageRequest.of(0, 10);
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        service.findAll(in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeRepository).findAll(captor.capture());
        Sort sort = captor.getValue().getSort();

        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("createdAt")).getDirection());
        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("id")).getDirection());
    }

    @Test
    void stream_noCursor_usesFindAllBy() {
        Slice<Employee> slice = new SliceImpl<>(List.of(), PageRequest.of(0, 1), false);
        when(employeeRepository.findAllBy(any())).thenReturn(slice);
        service.stream(null, 2);
        verify(employeeRepository).findAllBy(any());
    }
}
