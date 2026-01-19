package com.khasanshin.employmentservice;

import com.khasanshin.employmentservice.application.EmploymentApplicationService;
import com.khasanshin.employmentservice.domain.model.Employment;
import com.khasanshin.employmentservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.employmentservice.domain.port.EmploymentRepositoryPort;
import com.khasanshin.employmentservice.domain.port.OrgVerifierPort;
import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import com.khasanshin.employmentservice.mapper.EmploymentMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmploymentServiceTest {

    @Mock EmploymentRepositoryPort repo;
    @Mock EmploymentMapper mapper;
    @Mock OrgVerifierPort orgVerifier;
    @Mock EmployeeVerifierPort employeeVerifier;
    @Mock org.springframework.transaction.support.TransactionTemplate tx;

    EmploymentApplicationService service;

    @BeforeEach
    void setUp() {
        when(tx.execute(any())).thenAnswer(inv -> ((org.springframework.transaction.support.TransactionCallback<?>) inv.getArgument(0)).doInTransaction(null));
        service = new EmploymentApplicationService(repo, mapper, tx, orgVerifier, employeeVerifier);
    }

    @Test
    void create_validatesAndSaves() {
        UUID emp = UUID.randomUUID();
        UUID dept = UUID.randomUUID();
        UUID pos = UUID.randomUUID();
        CreateEmploymentDto dto = CreateEmploymentDto.builder()
                .employeeId(emp).departmentId(dept).positionId(pos)
                .startDate(LocalDate.now())
                .build();

        Employment toSave = Employment.builder()
                .employeeId(emp).departmentId(dept).positionId(pos)
                .startDate(dto.getStartDate()).status(Employment.Status.ACTIVE).rate(BigDecimal.ONE)
                .build();
        Employment saved = toSave.toBuilder().id(UUID.randomUUID()).build();

        when(repo.findOverlaps(emp, dept, pos, dto.getStartDate(), null)).thenReturn(List.of());
        when(mapper.toDomain(dto)).thenReturn(toSave);
        when(repo.save(toSave)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(EmploymentDto.builder().id(saved.getId()).build());

        EmploymentDto out = service.create(dto).block();

        assertNotNull(out);
        verify(employeeVerifier).ensureEmployeeExists(emp);
        verify(orgVerifier).ensureDepartmentExists(dept);
        verify(orgVerifier).ensurePositionExists(pos);
    }

    @Test
    void get_notFound_throws404() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()).block());
    }

    @Test
    void close_updatesStatus() {
        UUID id = UUID.randomUUID();
        Employment current = Employment.builder()
                .id(id).startDate(LocalDate.now().minusDays(10)).status(Employment.Status.ACTIVE).build();
        Employment closed = current.toBuilder().status(Employment.Status.CLOSED).endDate(LocalDate.now()).build();

        when(repo.findById(id)).thenReturn(Optional.of(current));
        when(repo.save(any())).thenReturn(closed);
        when(mapper.toDto(closed)).thenReturn(EmploymentDto.builder().id(id).status(Employment.Status.CLOSED).build());

        EmploymentDto out = service.close(id, CloseEmploymentDto.builder().build()).block();

        assertEquals(Employment.Status.CLOSED, out.getStatus());
    }

    @Test
    void listByDepartment_delegates() {
        UUID dept = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(repo.findByDepartmentIdAndStatus(eq(dept), eq(Employment.Status.ACTIVE), any()))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));
        service.listByDepartment(dept, true, 0, 10).collectList().block();
        verify(repo).findByDepartmentIdAndStatus(eq(dept), eq(Employment.Status.ACTIVE), any());
    }
}
