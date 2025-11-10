package com.khasanshin.employmentservice;

import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import com.khasanshin.employmentservice.entity.Employment;
import com.khasanshin.employmentservice.exception.RemoteServiceUnavailableException;
import com.khasanshin.employmentservice.feign.EmployeeClient;
import com.khasanshin.employmentservice.feign.OrgClient;
import com.khasanshin.employmentservice.mapper.EmploymentMapper;
import com.khasanshin.employmentservice.repository.EmploymentRepository;
import com.khasanshin.employmentservice.service.EmploymentService;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmploymentServiceTest {

    @Mock EmploymentRepository repo;
    @Mock EmploymentMapper mapper;
    @Mock TransactionTemplate tx;
    @Mock EmployeeClient employeeClient;
    @Mock OrgClient orgClient;

    EmploymentService service;

    @BeforeEach
    void setUp() {
        service = new EmploymentService(repo, mapper, tx, employeeClient, orgClient);

        lenient().when(tx.execute(any()))
                .thenAnswer(inv -> {
                    TransactionCallback<Object> cb = inv.getArgument(0);
                    return cb.doInTransaction(null);
                });
    }

    private Employment employment(UUID id, UUID empId, UUID depId, UUID posId, LocalDate start, LocalDate end) {
        Employment e = new Employment();
        e.setId(id);
        e.setEmployeeId(empId);
        e.setDepartmentId(depId);
        e.setPositionId(posId);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setStatus(Employment.Status.ACTIVE);
        return e;
    }

    private EmploymentDto dtoFromEntity(Employment e) {
        return EmploymentDto.builder()
                .id(e.getId())
                .employeeId(e.getEmployeeId())
                .departmentId(e.getDepartmentId())
                .positionId(e.getPositionId())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .status(e.getStatus())
                .rate(e.getRate())
                .build();
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), null);
        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        EmploymentDto out = service.get(id).block();

        assertNotNull(out);
        verify(repo).findById(id);
        verify(mapper).toDto(e);
    }

    @Test
    void get_notFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()).block());
    }

    @Test
    void create_ok_validatesAll_setsDefaultRate_andSaves() {
        UUID empId = UUID.randomUUID();
        UUID depId = UUID.randomUUID();
        UUID posId = UUID.randomUUID();

        var dto = CreateEmploymentDto.builder()
                .employeeId(empId).departmentId(depId).positionId(posId)
                .startDate(LocalDate.of(2024,1,1))
                .build();

        when(employeeClient.exists(empId)).thenReturn(ResponseEntity.ok().build());
        when(orgClient.departmentExists(depId)).thenReturn(ResponseEntity.ok().build());
        when(orgClient.positionExists(posId)).thenReturn(ResponseEntity.ok().build());

        when(repo.findOverlaps(empId, depId, posId, dto.getStartDate(), null)).thenReturn(List.of());

        Employment toSave = employment(UUID.randomUUID(), empId, depId, posId, dto.getStartDate(), null);
        toSave.setRate(null);
        when(mapper.toEntity(dto)).thenReturn(toSave);

        when(repo.saveAndFlush(any(Employment.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Employment savedAfter = employment(toSave.getId(), empId, depId, posId, dto.getStartDate(), null);
        savedAfter.setRate(BigDecimal.valueOf(1.00));
        when(mapper.toDto(any(Employment.class)))
                .thenAnswer(inv -> dtoFromEntity(inv.getArgument(0)));

        EmploymentDto out = service.create(dto).block();

        assertNotNull(out);
        assertEquals(BigDecimal.valueOf(1.00), out.getRate());

        ArgumentCaptor<Employment> captor = ArgumentCaptor.forClass(Employment.class);
        verify(repo).saveAndFlush(captor.capture());
        assertEquals(BigDecimal.valueOf(1.00), captor.getValue().getRate());

        verify(repo).findOverlaps(empId, depId, posId, dto.getStartDate(), null);
        verify(employeeClient).exists(empId);
        verify(orgClient).departmentExists(depId);
        verify(orgClient).positionExists(posId);
    }

    @Test
    void create_overlaps_throwsIllegalState() {
        var dto = CreateEmploymentDto.builder()
                .employeeId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .startDate(LocalDate.of(2024,1,1))
                .build();

        when(employeeClient.exists(dto.getEmployeeId())).thenReturn(ResponseEntity.ok().build());
        when(orgClient.departmentExists(dto.getDepartmentId())).thenReturn(ResponseEntity.ok().build());
        when(orgClient.positionExists(dto.getPositionId())).thenReturn(ResponseEntity.ok().build());
        when(repo.findOverlaps(any(), any(), any(), any(), isNull()))
                .thenReturn(List.of(new Employment()));

        assertThrows(IllegalStateException.class, () -> service.create(dto).block());
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void create_employeeNotFound_maps404() {
        var dto = CreateEmploymentDto.builder()
                .employeeId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .startDate(LocalDate.now())
                .build();

        doThrow(mock(FeignException.NotFound.class)).when(employeeClient).exists(dto.getEmployeeId());

        assertThrows(EntityNotFoundException.class, () -> service.create(dto).block());
    }

    @Test
    void create_departmentNotFound_maps404() {
        var dto = CreateEmploymentDto.builder()
                .employeeId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .startDate(LocalDate.now())
                .build();

        when(employeeClient.exists(dto.getEmployeeId())).thenReturn(ResponseEntity.ok().build());
        doThrow(mock(FeignException.NotFound.class)).when(orgClient).departmentExists(dto.getDepartmentId());

        assertThrows(EntityNotFoundException.class, () -> service.create(dto).block());
    }

    @Test
    void create_positionNotFound_maps404() {
        var dto = CreateEmploymentDto.builder()
                .employeeId(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .positionId(UUID.randomUUID())
                .startDate(LocalDate.now())
                .build();

        when(employeeClient.exists(dto.getEmployeeId())).thenReturn(ResponseEntity.ok().build());
        when(orgClient.departmentExists(dto.getDepartmentId())).thenReturn(ResponseEntity.ok().build());
        doThrow(mock(FeignException.NotFound.class)).when(orgClient).positionExists(dto.getPositionId());

        assertThrows(EntityNotFoundException.class, () -> service.create(dto).block());
    }

    @Test
    void update_ok_updatesAndSaves() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2024,1,1), null);

        when(repo.findById(id)).thenReturn(Optional.of(e));

        UpdateEmploymentDto dto = UpdateEmploymentDto.builder()
                .endDate(LocalDate.of(2024, 12, 31))
                .rate(BigDecimal.valueOf(0.5))
                .build();

        doAnswer(inv -> {
            UpdateEmploymentDto d = inv.getArgument(0);
            Employment target = inv.getArgument(1);
            if (d.getEndDate() != null) target.setEndDate(d.getEndDate());
            if (d.getRate() != null) target.setRate(d.getRate());
            return null;
        }).when(mapper).updateEntity(eq(dto), eq(e));

        when(repo.saveAndFlush(e)).thenReturn(e);
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        EmploymentDto out = service.update(id, dto).block();

        assertNotNull(out);
        assertEquals(LocalDate.of(2024, 12, 31), e.getEndDate());
        assertEquals(BigDecimal.valueOf(0.5), e.getRate());
    }

    @Test
    void update_endBeforeStart_throwsIllegalArgument() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2024,5,1), null);

        when(repo.findById(id)).thenReturn(Optional.of(e));

        UpdateEmploymentDto dto = UpdateEmploymentDto.builder()
                .endDate(LocalDate.of(2024, 4, 30))
                .build();

        doAnswer(inv -> {
            Employment target = inv.getArgument(1);
            target.setEndDate(LocalDate.of(2024,4,30));
            return null;
        }).when(mapper).updateEntity(eq(dto), eq(e));

        assertThrows(IllegalArgumentException.class, () -> service.update(id, dto).block());
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void update_notFound_throws404() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.update(UUID.randomUUID(),
                UpdateEmploymentDto.builder().build()).block());
    }

    @Test
    void close_ok_withBodyEndDate_setsClosedAndSaves() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2024,1,1), null);

        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(repo.saveAndFlush(e)).thenReturn(e);
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        LocalDate end = LocalDate.of(2024, 6, 1);
        EmploymentDto out = service.close(id, CloseEmploymentDto.builder().endDate(end).build()).block();

        assertNotNull(out);
        assertEquals(Employment.Status.CLOSED, e.getStatus());
        assertEquals(end, e.getEndDate());
    }

    @Test
    void close_ok_defaultsEndDateToNow_whenBodyNull() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now().minusDays(1), null);

        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(repo.saveAndFlush(e)).thenReturn(e);
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        EmploymentDto out = service.close(id, null).block();

        assertNotNull(out);
        assertEquals(Employment.Status.CLOSED, e.getStatus());
        assertNotNull(e.getEndDate());
    }

    @Test
    void close_alreadyClosed_returnsAsIs() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2024,1,1), LocalDate.of(2024,2,1));
        e.setStatus(Employment.Status.CLOSED);

        when(repo.findById(id)).thenReturn(Optional.of(e));
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        EmploymentDto out = service.close(id, CloseEmploymentDto.builder().endDate(LocalDate.of(2024,3,1)).build()).block();

        assertNotNull(out);
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void close_endBeforeStart_throwsIllegalArgument() {
        UUID id = UUID.randomUUID();
        Employment e = employment(id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2024,5,1), null);

        when(repo.findById(id)).thenReturn(Optional.of(e));

        assertThrows(IllegalArgumentException.class,
                () -> service.close(id, CloseEmploymentDto.builder().endDate(LocalDate.of(2024,4,30)).build()).block());
        verify(repo, never()).saveAndFlush(any());
    }

    @Test
    void close_notFound_throws404() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.close(UUID.randomUUID(), null).block());
    }

    @Test
    void listByEmployee_returnsFluxOfDtos_fromJpaPage() {
        UUID empId = UUID.randomUUID();
        int page = 0, size = 10;
        Employment e = employment(UUID.randomUUID(), empId, UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), null);

        when(repo.findByEmployeeIdOrderByStartDateDesc(eq(empId), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    return new PageImpl<>(List.of(e), p, 1);
                });
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        var list = service.listByEmployee(empId, page, size).collectList().block();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(empId, list.getFirst().getEmployeeId());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findByEmployeeIdOrderByStartDateDesc(eq(empId), captor.capture());
        assertEquals(PageRequest.of(page, size), captor.getValue());
    }

    @Test
    void listByDepartment_activeTrue_usesActiveQuery_andReturnsFlux() {
        UUID dep = UUID.randomUUID();
        int page = 0, size = 5;
        Employment e = employment(UUID.randomUUID(), UUID.randomUUID(), dep, UUID.randomUUID(), LocalDate.now(), null);

        when(repo.findByDepartmentIdAndStatus(eq(dep), eq(Employment.Status.ACTIVE), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2);
                    return new PageImpl<>(List.of(e), p, 1);
                });
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        var list = service.listByDepartment(dep, true, page, size).collectList().block();

        assertNotNull(list);
        assertEquals(1, list.size());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findByDepartmentIdAndStatus(eq(dep), eq(Employment.Status.ACTIVE), captor.capture());
        assertEquals(PageRequest.of(page, size), captor.getValue());
        verify(repo, never()).findByDepartmentId(any(), any());
    }

    @Test
    void listByDepartment_activeFalse_usesAllQuery_andReturnsFlux() {
        UUID dep = UUID.randomUUID();
        int page = 0, size = 5;
        Employment e = employment(UUID.randomUUID(), UUID.randomUUID(), dep, UUID.randomUUID(), LocalDate.now(), null);

        when(repo.findByDepartmentId(eq(dep), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1);
                    return new PageImpl<>(List.of(e), p, 1);
                });
        when(mapper.toDto(e)).thenReturn(dtoFromEntity(e));

        var list = service.listByDepartment(dep, false, page, size).collectList().block();

        assertNotNull(list);
        assertEquals(1, list.size());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repo).findByDepartmentId(eq(dep), captor.capture());
        assertEquals(PageRequest.of(page, size), captor.getValue());
        verify(repo, never()).findByDepartmentIdAndStatus(any(), any(), any());
    }

    @Test
    void countByEmployee_returnsTotal() {
        UUID empId = UUID.randomUUID();
        when(repo.countByEmployeeId(empId)).thenReturn(42L);

        Long total = service.countByEmployee(empId).block();

        assertEquals(42L, total);
        verify(repo).countByEmployeeId(empId);
    }

    @Test
    void countByDepartment_activeVariants() {
        UUID dep = UUID.randomUUID();

        when(repo.countByDepartmentIdAndStatus(dep, Employment.Status.ACTIVE)).thenReturn(10L);
        when(repo.countByDepartmentId(dep)).thenReturn(15L);

        Long active = service.countByDepartment(dep, true).block();
        Long all = service.countByDepartment(dep, false).block();

        assertEquals(10L, active);
        assertEquals(15L, all);
        verify(repo).countByDepartmentIdAndStatus(dep, Employment.Status.ACTIVE);
        verify(repo).countByDepartmentId(dep);
    }

    @Test
    void employeeExistsUnavailable_throws503() {
        assertThrows(RemoteServiceUnavailableException.class,
                () -> service.employeeExistsUnavailable(UUID.randomUUID(), new RuntimeException("x")));
    }

    @Test
    void departmentExistsUnavailable_throws503() {
        assertThrows(RemoteServiceUnavailableException.class,
                () -> service.departmentExistsUnavailable(UUID.randomUUID(), new RuntimeException("x")));
    }

    @Test
    void positionExistsUnavailable_throws503() {
        assertThrows(RemoteServiceUnavailableException.class,
                () -> service.positionExistsUnavailable(UUID.randomUUID(), new RuntimeException("x")));
    }
}
