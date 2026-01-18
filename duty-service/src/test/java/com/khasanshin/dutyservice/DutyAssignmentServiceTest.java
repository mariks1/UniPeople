package com.khasanshin.dutyservice;

import com.khasanshin.dutyservice.application.DutyAssignmentApplicationService;
import com.khasanshin.dutyservice.domain.model.DutyAssignment;
import com.khasanshin.dutyservice.domain.port.DutyAssignmentRepositoryPort;
import com.khasanshin.dutyservice.domain.port.DutyRepositoryPort;
import com.khasanshin.dutyservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.dutyservice.domain.port.OrgVerifierPort;
import com.khasanshin.dutyservice.dto.AssignDutyDto;
import com.khasanshin.dutyservice.dto.DutyAssignmentDto;
import com.khasanshin.dutyservice.mapper.DutyAssignmentMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DutyAssignmentServiceTest {

    @Mock DutyAssignmentRepositoryPort assignmentRepo;
    @Mock DutyAssignmentMapper mapper;
    @Mock DutyRepositoryPort dutyRepo;
    @Mock EmployeeVerifierPort employeeVerifier;
    @Mock OrgVerifierPort orgVerifier;

    DutyAssignmentApplicationService service;

    @BeforeEach
    void setUp() {
        service = new DutyAssignmentApplicationService(assignmentRepo, mapper, dutyRepo, employeeVerifier, orgVerifier);
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

        DutyAssignment saved = DutyAssignment.builder()
                .id(UUID.randomUUID()).departmentId(dept).employeeId(emp).dutyId(duty).build();

        when(assignmentRepo.save(any(DutyAssignment.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(DutyAssignmentDto.builder().build());

        assertNotNull(service.assign(dept, req));

        verify(orgVerifier).ensureDepartmentExists(dept);
        verify(employeeVerifier).ensureEmployeeExists(emp);
        verify(dutyRepo).existsById(duty);
        verify(assignmentRepo).save(any(DutyAssignment.class));
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

        DutyAssignment saved = DutyAssignment.builder()
                .id(UUID.randomUUID()).departmentId(dept).employeeId(emp).dutyId(duty).assignedBy(by).build();
        when(assignmentRepo.save(any(DutyAssignment.class))).thenReturn(saved);
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
        verify(assignmentRepo, never()).save(any());
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

        DutyAssignment a = DutyAssignment.builder()
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

        DutyAssignment a = DutyAssignment.builder()
                .id(id).departmentId(UUID.randomUUID()).build();

        when(assignmentRepo.findById(id)).thenReturn(Optional.of(a));

        assertThrows(EntityNotFoundException.class, () -> service.unassign(dept, id));
        verify(assignmentRepo, never()).delete(any());
    }
}
