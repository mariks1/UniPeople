package com.khasanshin.leaveservice;

import com.khasanshin.leaveservice.application.LeaveRequestApplicationService;
import com.khasanshin.leaveservice.application.LeaveTypeApplicationService;
import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import com.khasanshin.leaveservice.domain.model.LeaveType;
import com.khasanshin.leaveservice.domain.port.LeaveRequestRepositoryPort;
import com.khasanshin.leaveservice.domain.port.LeaveTypeRepositoryPort;
import com.khasanshin.leaveservice.dto.*;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock LeaveTypeRepositoryPort leaveTypeRepository;
    @Mock LeaveRequestRepositoryPort leaveRequestRepository;
    @Mock LeaveMapper leaveMapper;

    LeaveRequestApplicationService requestService;
    LeaveTypeApplicationService typeService;

    @BeforeEach
    void setUp() {
        requestService = new LeaveRequestApplicationService(leaveTypeRepository, leaveRequestRepository, leaveMapper);
        typeService = new LeaveTypeApplicationService(leaveTypeRepository, leaveMapper);
    }

    private LeaveType type(UUID id, String code, Integer maxPerYear) {
        return LeaveType.builder()
                .id(id)
                .code(code)
                .maxDaysPerYear(maxPerYear)
                .build();
    }

    private LeaveTypeDto typeDto(LeaveType t) {
        return LeaveTypeDto.builder()
                .id(t.getId())
                .code(t.getCode())
                .maxDaysPerYear(t.getMaxDaysPerYear())
                .build();
    }

    private LeaveRequest req(UUID id, UUID emp, UUID typeId, LocalDate from, LocalDate to, LeaveRequest.Status st) {
        return LeaveRequest.builder()
                .id(id)
                .employeeId(emp)
                .typeId(typeId)
                .dateFrom(from)
                .dateTo(to)
                .status(st)
                .build();
    }

    private LeaveRequestDto reqDto(LeaveRequest r) {
        return LeaveRequestDto.builder()
                .id(r.getId())
                .employeeId(r.getEmployeeId())
                .typeId(r.getTypeId())
                .dateFrom(r.getDateFrom())
                .dateTo(r.getDateTo())
                .status(r.getStatus())
                .comment(r.getComment())
                .approverId(r.getApproverId())
                .build();
    }

    @Test
    void countTypes_ok() {
        when(leaveTypeRepository.count()).thenReturn(Mono.just(42L));
        assertEquals(42L, typeService.countTypes().block());
    }

    @Test
    void listTypes_capsPageSizeAndMaps() {
        Pageable in = PageRequest.of(1, 500, Sort.by("code"));
        LeaveType t = type(UUID.randomUUID(), "VAC", 28);
        when(leaveTypeRepository.findAll(any(Pageable.class))).thenReturn(Flux.just(t));
        when(leaveMapper.toDto(t)).thenReturn(typeDto(t));

        List<LeaveTypeDto> out = typeService.listTypes(in).collectList().block();

        assertEquals(1, out.size());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveTypeRepository).findAll(captor.capture());
        Pageable passed = captor.getValue();
        assertEquals(1, passed.getPageNumber());
        assertEquals(50, passed.getPageSize());
        assertEquals(Sort.by("code"), passed.getSort());
    }

    @Test
    void createType_ok_saves() {
        CreateLeaveTypeDto dto = CreateLeaveTypeDto.builder().code("SICK").build();
        when(leaveTypeRepository.existsByCodeIgnoreCase("SICK")).thenReturn(Mono.just(false));

        LeaveType toSave = type(UUID.randomUUID(), "SICK", 10);
        when(leaveMapper.toDomain(dto)).thenReturn(toSave);
        when(leaveTypeRepository.save(toSave)).thenReturn(Mono.just(toSave));
        when(leaveMapper.toDto(toSave)).thenReturn(typeDto(toSave));

        LeaveTypeDto out = typeService.createType(dto).block();
        assertNotNull(out);
        verify(leaveTypeRepository).save(toSave);
    }

    @Test
    void createType_duplicate_precheck() {
        CreateLeaveTypeDto dto = CreateLeaveTypeDto.builder().code("VAC").build();
        when(leaveTypeRepository.existsByCodeIgnoreCase("VAC")).thenReturn(Mono.just(true));

        assertThrows(IllegalStateException.class, () -> typeService.createType(dto).block());
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void updateType_ok_updatesAndSaves() {
        UUID id = UUID.randomUUID();
        UpdateLeaveTypeDto dto = UpdateLeaveTypeDto.builder().name("Vacation").build();

        LeaveType existing = type(id, "VAC", 28);
        LeaveType updated = existing.toBuilder().name("Vacation").build();
        when(leaveTypeRepository.findById(id)).thenReturn(Mono.just(existing));
        when(leaveMapper.updateLeaveType(dto, existing)).thenReturn(updated);
        when(leaveTypeRepository.update(updated)).thenReturn(Mono.just(updated));
        when(leaveMapper.toDto(updated)).thenReturn(typeDto(updated));

        LeaveTypeDto out = typeService.updateType(id, dto).block();
        assertNotNull(out);
        verify(leaveTypeRepository).update(updated);
    }

    @Test
    void updateType_notFound_404() {
        when(leaveTypeRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> typeService.updateType(UUID.randomUUID(), UpdateLeaveTypeDto.builder().build()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteType_ok() {
        UUID id = UUID.randomUUID();
        when(leaveTypeRepository.findById(id)).thenReturn(Mono.just(type(id, "X", 1)));
        when(leaveTypeRepository.deleteById(id)).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> typeService.deleteType(id).block());
        verify(leaveTypeRepository).deleteById(id);
    }

    @Test
    void deleteType_notFound_404() {
        when(leaveTypeRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> typeService.deleteType(UUID.randomUUID()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(leaveTypeRepository, never()).deleteById((UUID) any());
    }

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        assertNotNull(requestService.get(id).block());
    }

    @Test
    void get_notFound_404() {
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> requestService.get(UUID.randomUUID()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void create_draft_ok_noLimitCheck() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 1, 10);
        LocalDate to = LocalDate.of(2024, 1, 12);

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(false)
                .build();

        LeaveType type = type(typeId, "VAC", 28);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));

        LeaveRequest entity = req(UUID.randomUUID(), emp, typeId, from, to, LeaveRequest.Status.DRAFT);
        when(leaveMapper.toDomain(dto)).thenReturn(entity);
        when(leaveRequestRepository.save(entity)).thenReturn(Mono.just(entity));
        when(leaveMapper.toDto(entity)).thenReturn(reqDto(entity));

        LeaveRequestDto out = requestService.create(dto).block();

        assertNotNull(out);
        verify(leaveTypeRepository).findById(typeId);
        verify(leaveRequestRepository).existsOverlaps(emp, from, to);
        verify(leaveRequestRepository, never()).sumApprovedDaysForYear(any(), any(), anyInt());
    }

    @Test
    void create_submit_checksYearLimit_ok() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 2, 1);
        LocalDate to = LocalDate.of(2024, 2, 3);

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(true)
                .build();

        LeaveType type = type(typeId, "VAC", 5);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(1));

        LeaveRequest entity = req(UUID.randomUUID(), emp, typeId, from, to, LeaveRequest.Status.DRAFT);
        when(leaveMapper.toDomain(dto)).thenReturn(entity);
        when(leaveRequestRepository.save(entity)).thenReturn(Mono.just(entity));
        when(leaveMapper.toDto(entity)).thenReturn(reqDto(entity));

        assertNotNull(requestService.create(dto).block());
    }

    @Test
    void create_submit_limitExceeded_throws() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 3, 5);

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(true)
                .build();

        LeaveType type = type(typeId, "VAC", 7);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(4));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> requestService.create(dto).block());
        assertTrue(ex.getMessage().contains("yearly limit exceeded"));
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void create_overlaps_throws() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 1, 2);

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId).dateFrom(from).dateTo(to).build();

        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type(typeId, "VAC", 10)));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(true));

        assertThrows(IllegalStateException.class, () -> requestService.create(dto).block());
    }

    @Test
    void create_typeNotFound_404() {
        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(UUID.randomUUID())
                .typeId(UUID.randomUUID())
                .dateFrom(LocalDate.now())
                .dateTo(LocalDate.now().plusDays(1))
                .build();

        when(leaveTypeRepository.findById(dto.getTypeId())).thenReturn(Mono.empty());

        when(leaveRequestRepository.existsOverlaps(eq(dto.getEmployeeId()),
                eq(dto.getDateFrom()), eq(dto.getDateTo()))).thenReturn(Mono.just(false));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> requestService.create(dto).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void create_badDateOrder_throwsImmediately() {
        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(UUID.randomUUID())
                .typeId(UUID.randomUUID())
                .dateFrom(LocalDate.of(2024, 5, 10))
                .dateTo(LocalDate.of(2024, 5, 1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> requestService.create(dto).block());
        verifyNoInteractions(leaveTypeRepository, leaveRequestRepository);
    }

    @Test
    void update_ok_fromPending_validatesAndSaves() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        LeaveRequest existing = req(id, emp, typeId,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 3), LeaveRequest.Status.PENDING);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        UpdateLeaveRequestDto dto = UpdateLeaveRequestDto.builder()
                .dateTo(LocalDate.of(2024, 4, 4))
                .comment("upd")
                .build();

        LeaveRequest updated = existing.toBuilder()
                .dateTo(LocalDate.of(2024, 4, 4))
                .comment("upd")
                .build();
        when(leaveMapper.updateLeave(dto, existing)).thenReturn(updated);

        when(leaveRequestRepository.existsOverlapsExcluding(id, emp,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 4)))
                .thenReturn(Mono.just(false));

        when(leaveRequestRepository.save(updated)).thenReturn(Mono.just(updated));
        when(leaveMapper.toDto(updated)).thenReturn(reqDto(updated));

        LeaveRequestDto out = requestService.update(id, dto).block();

        assertNotNull(out);
        assertEquals(LocalDate.of(2024, 4, 4), updated.getDateTo());
        assertEquals("upd", updated.getComment());
    }

    @Test
    void update_statusNotAllowed_throws() {
        UUID id = UUID.randomUUID();
        LeaveRequest existing = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.APPROVED);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> requestService.update(id, UpdateLeaveRequestDto.builder().build()).block());
        assertTrue(ex.getMessage().contains("only DRAFT/PENDING"));
    }

    @Test
    void update_overlapsExcluding_throws() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        LeaveRequest existing = req(id, emp, UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        when(leaveMapper.updateLeave(any(UpdateLeaveRequestDto.class), eq(existing))).thenReturn(existing);

        when(leaveRequestRepository.existsOverlapsExcluding(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(requestService.update(id, UpdateLeaveRequestDto.builder().build()))
                .expectErrorSatisfies(err -> assertTrue(err instanceof IllegalStateException))
                .verify();
    }

    @Test
    void update_notFound_404() {
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> requestService.update(UUID.randomUUID(), UpdateLeaveRequestDto.builder().build()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void approve_ok_checksLimit_andSavesApproved() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 6, 10);
        LocalDate to = LocalDate.of(2024, 6, 12);

        LeaveRequest e = req(id, emp, typeId, from, to, LeaveRequest.Status.PENDING);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        LeaveType type = type(typeId, "VAC", 10);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(6));

        LeaveRequest approved = e.toBuilder().status(LeaveRequest.Status.APPROVED).approverId(UUID.randomUUID()).comment("ok").build();
        when(leaveRequestRepository.save(any())).thenReturn(Mono.just(approved));
        when(leaveMapper.toDto(approved)).thenAnswer(inv -> reqDto(approved));

        DecisionDto d = DecisionDto.builder().approverId(approved.getApproverId()).comment("ok").build();

        LeaveRequestDto out = requestService.approve(id, d).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.APPROVED, approved.getStatus());
        assertEquals(d.getApproverId(), approved.getApproverId());
        assertEquals("ok", approved.getComment());
    }

    @Test
    void approve_notPending_throws() {
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.just(e));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> requestService.approve(UUID.randomUUID(), DecisionDto.builder().build()).block());
        assertTrue(ex.getMessage().contains("not PENDING"));
    }

    @Test
    void approve_limitExceeded_throws() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        LeaveRequest e = req(id, emp, typeId,
                LocalDate.of(2024,7,1), LocalDate.of(2024,7,4), LeaveRequest.Status.PENDING);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        LeaveType type = type(typeId, "VAC", 7);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(4));

        assertThrows(IllegalStateException.class, () -> requestService.approve(id, DecisionDto.builder().build()).block());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void reject_ok_setsRejected() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.PENDING);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        when(leaveRequestRepository.save(any()))
                .thenAnswer(inv -> Mono.just((LeaveRequest) inv.getArgument(0)));
        when(leaveMapper.toDto(any(LeaveRequest.class)))
                .thenAnswer(inv -> reqDto((LeaveRequest) inv.getArgument(0)));

        DecisionDto d = DecisionDto.builder().approverId(UUID.randomUUID()).comment("nope").build();
        LeaveRequestDto out = requestService.reject(id, d).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.REJECTED, out.getStatus());
        assertEquals("nope", out.getComment());
        assertEquals(d.getApproverId(), out.getApproverId());
    }

    @Test
    void cancel_ok_fromApproved() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.APPROVED);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        LeaveRequest canceled = e.toBuilder().status(LeaveRequest.Status.CANCELED).build();
        when(leaveRequestRepository.save(any())).thenReturn(Mono.just(canceled));
        when(leaveMapper.toDto(canceled)).thenReturn(reqDto(canceled));

        LeaveRequestDto out = requestService.cancel(id).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.CANCELED, canceled.getStatus());
    }

    @Test
    void cancel_badStatus_throws() {
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.just(e));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> requestService.cancel(e.getId()).block());
        assertTrue(ex.getMessage().contains("only PENDING/APPROVED"));
    }

    @Test
    void countLeaveByEmployee_ok() {
        UUID emp = UUID.randomUUID();
        when(leaveRequestRepository.countByEmployeeId(emp)).thenReturn(Mono.just(7L));
        assertEquals(7L, requestService.countLeaveByEmployee(emp).block());
    }

    @Test
    void listByEmployee_mapsAndKeepsPageable() {
        UUID emp = UUID.randomUUID();
        Pageable p = PageRequest.of(0, 10, Sort.by(Sort.Order.desc("dateFrom")));

        LeaveRequest e = req(UUID.randomUUID(), emp, UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);

        when(leaveRequestRepository.findByEmployeeIdOrderByDateFromDesc(eq(emp), any(Pageable.class)))
                .thenReturn(Flux.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        List<LeaveRequestDto> list = requestService.listByEmployee(emp, p).collectList().block();

        assertEquals(1, list.size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveRequestRepository).findByEmployeeIdOrderByDateFromDesc(eq(emp), captor.capture());
        assertEquals(p, captor.getValue());
    }

    @Test
    void countLeaveByStatus_ok() {
        when(leaveRequestRepository.countByStatus(LeaveRequest.Status.PENDING)).thenReturn(Mono.just(3L));
        assertEquals(3L, requestService.countLeaveByStatus(LeaveRequest.Status.PENDING).block());
    }

    @Test
    void listByStatus_mapsAndKeepsPageable() {
        Pageable p = PageRequest.of(1, 5, Sort.by("dateFrom"));
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(2), LeaveRequest.Status.PENDING);

        when(leaveRequestRepository.findByStatus(eq(LeaveRequest.Status.PENDING), any(Pageable.class)))
                .thenReturn(Flux.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        List<LeaveRequestDto> list = requestService.listByStatus(LeaveRequest.Status.PENDING, p).collectList().block();

        assertEquals(1, list.size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveRequestRepository).findByStatus(eq(LeaveRequest.Status.PENDING), captor.capture());
        assertEquals(p, captor.getValue());
    }
}
