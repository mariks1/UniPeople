package com.khasanshin.dutyservice;

import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.entity.DepartmentDutyAssignment;
import com.khasanshin.dutyservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.dutyservice.feign.EmployeeClient;
import com.khasanshin.dutyservice.feign.EmployeeVerifier;
import com.khasanshin.dutyservice.feign.OrgClient;
import com.khasanshin.dutyservice.feign.OrgVerifier;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
import com.khasanshin.dutyservice.repository.DutyAssignmentRepository;
import com.khasanshin.dutyservice.repository.DutyRepository;
import com.khasanshin.dutyservice.service.DutyAssignmentService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DutyAssignmentServiceTest {

    @Mock DutyAssignmentRepository assignmentRepo;
    @Mock DutyAssignmentMapper mapper;
    @Mock DutyRepository dutyRepo;
    @Mock
    EmployeeVerifier employeeVerifier;
    @Mock
    OrgVerifier orgVerifier;


    DutyAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new DutyAssignmentService(assignmentRepo, mapper, dutyRepo, employeeVerifier, orgVerifier);
    }

    @Test
    void assign_ok_validatesAndSaves() {
        UUID dept = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID duty = UUID.randomUUID();

        AssignDutyDto req = AssignDutyDto.builder()
                .employeeId(emp)
                .dutyId(duty)
                .note("n")
                .assignedBy(null)
                .build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(dept, emp, duty)).thenReturn(false);
        doNothing().when(orgVerifier).ensureDepartmentExists(dept);
        doNothing().when(employeeVerifier).ensureEmployeeExists(emp);
        when(dutyRepo.existsById(duty)).thenReturn(true);

        DepartmentDutyAssignment saved = DepartmentDutyAssignment.builder()
                .id(UUID.randomUUID()).departmentId(dept).employeeId(emp).dutyId(duty).build();

        when(assignmentRepo.saveAndFlush(any(DepartmentDutyAssignment.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(DutyAssignmentDto.builder().build());

        assertNotNull(service.assign(dept, req));

        verify(orgVerifier).ensureDepartmentExists(dept);
        verify(employeeVerifier).ensureEmployeeExists(emp);
        verify(dutyRepo).existsById(duty);
        verify(assignmentRepo).saveAndFlush(any(DepartmentDutyAssignment.class));
    }

    @Test
    void assign_rejectsDuplicate() {
        UUID dept = UUID.randomUUID();
        AssignDutyDto req = AssignDutyDto.builder()
                .employeeId(UUID.randomUUID())
                .dutyId(UUID.randomUUID())
                .build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(eq(dept), any(), any()))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.assign(dept, req));
        verifyNoInteractions(orgVerifier, employeeVerifier, dutyRepo);
    }

    @Test
    void assign_departmentNotFound_maps404() {
        UUID dept = UUID.randomUUID();
        AssignDutyDto req = AssignDutyDto.builder()
                .employeeId(UUID.randomUUID())
                .dutyId(UUID.randomUUID())
                .build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(eq(dept), any(), any()))
                .thenReturn(false);
        doThrow(new EntityNotFoundException("dept not found"))
                .when(orgVerifier).ensureDepartmentExists(dept);

        assertThrows(EntityNotFoundException.class, () -> service.assign(dept, req));
        verify(employeeVerifier, never()).ensureEmployeeExists(any());
        verify(dutyRepo, never()).existsById(any());
    }

    @Test
    void assign_employeeNotFound_maps404() {
        UUID dept = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID duty = UUID.randomUUID();

        AssignDutyDto req = AssignDutyDto.builder()
                .employeeId(emp)
                .dutyId(duty)
                .build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(dept, emp, duty)).thenReturn(false);
        doNothing().when(orgVerifier).ensureDepartmentExists(dept);
        doThrow(new EntityNotFoundException("emp not found"))
                     .when(employeeVerifier).ensureEmployeeExists(emp);

        assertThrows(EntityNotFoundException.class, () -> service.assign(dept, req));
        verify(dutyRepo, never()).existsById(any());
    }

    @Test
    void assign_assignedByAlsoValidated_whenPresent() {
        UUID dept = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID by = UUID.randomUUID();
        UUID duty = UUID.randomUUID();

        AssignDutyDto req = AssignDutyDto.builder()
                .employeeId(emp).assignedBy(by).dutyId(duty).build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(dept, emp, duty)).thenReturn(false);
        doNothing().when(orgVerifier).ensureDepartmentExists(dept);
        doNothing().when(employeeVerifier).ensureEmployeeExists(emp);
        doNothing().when(employeeVerifier).ensureEmployeeExists(by);
        when(dutyRepo.existsById(duty)).thenReturn(true);

        DepartmentDutyAssignment saved = DepartmentDutyAssignment.builder()
                .id(UUID.randomUUID()).departmentId(dept).employeeId(emp).dutyId(duty).assignedBy(by).build();
        when(assignmentRepo.saveAndFlush(any(DepartmentDutyAssignment.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(DutyAssignmentDto.builder().build());

        assertNotNull(service.assign(dept, req));

        verify(employeeVerifier).ensureEmployeeExists(by);
    }

    @Test
    void assign_dutyNotFound_maps404() {
        UUID dept = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID duty = UUID.randomUUID();

        AssignDutyDto req = AssignDutyDto.builder().employeeId(emp).dutyId(duty).build();

        when(assignmentRepo.existsByDepartmentIdAndEmployeeIdAndDutyId(dept, emp, duty)).thenReturn(false);
        doNothing().when(orgVerifier).ensureDepartmentExists(dept);
        doNothing().when(employeeVerifier).ensureEmployeeExists(emp);
        when(dutyRepo.existsById(duty)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> service.assign(dept, req));
        verify(assignmentRepo, never()).saveAndFlush(any());
    }

    @Test
    void list_defaultSort_whenUnsorted() {
        UUID dept = UUID.randomUUID();
        Pageable in = PageRequest.of(0, 20);
        when(assignmentRepo.findByDepartmentId(any(), any(Pageable.class))).thenReturn(Page.empty());

        service.list(dept, in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assignmentRepo).findByDepartmentId(eq(dept), captor.capture());
        Sort sort = captor.getValue().getSort();
        assertEquals(Sort.Direction.DESC, Objects.requireNonNull(sort.getOrderFor("assignedAt")).getDirection());
    }

    @Test
    void list_keepsIncomingSort_whenProvided() {
        UUID dept = UUID.randomUUID();
        Pageable in = PageRequest.of(2, 5, Sort.by(Sort.Order.asc("employeeId")));
        when(assignmentRepo.findByDepartmentId(dept, in)).thenReturn(Page.empty());

        service.list(dept, in);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(assignmentRepo).findByDepartmentId(eq(dept), captor.capture());
        assertEquals(Sort.by(Sort.Order.asc("employeeId")), captor.getValue().getSort());
    }

    @Test
    void unassign_ok() {
        UUID dept = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        DepartmentDutyAssignment a = DepartmentDutyAssignment.builder()
                .id(id).departmentId(dept).build();

        when(assignmentRepo.findById(id)).thenReturn(Optional.of(a));

        service.unassign(dept, id);

        verify(assignmentRepo).delete(a);
    }

    @Test
    void unassign_404_whenNotFound() {
        when(assignmentRepo.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.unassign(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    void unassign_404_whenWrongDepartment() {
        UUID dept = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        DepartmentDutyAssignment a = DepartmentDutyAssignment.builder()
                .id(id).departmentId(UUID.randomUUID()).build();

        when(assignmentRepo.findById(id)).thenReturn(Optional.of(a));

        assertThrows(EntityNotFoundException.class, () -> service.unassign(dept, id));
        verify(assignmentRepo, never()).delete(any());
    }

}
