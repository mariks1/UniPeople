package com.khasanshin.leaveservice;

import com.khasanshin.leaveservice.dto.*;
import com.khasanshin.leaveservice.entity.LeaveRequest;
import com.khasanshin.leaveservice.entity.LeaveType;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
import com.khasanshin.leaveservice.repository.LeaveRequestRepository;
import com.khasanshin.leaveservice.repository.LeaveTypeRepository;
import com.khasanshin.leaveservice.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock LeaveTypeRepository leaveTypeRepository;
    @Mock LeaveRequestRepository leaveRequestRepository;
    @Mock LeaveMapper leaveMapper;

    LeaveService service;

    @BeforeEach
    void setUp() {
        service = new LeaveService(leaveTypeRepository, leaveRequestRepository, leaveMapper);
    }

    // -------- helpers

    private LeaveType type(UUID id, String code, Integer maxPerYear) {
        LeaveType t = new LeaveType();
        t.setId(id);
        t.setCode(code);
        t.setMaxDaysPerYear(maxPerYear);
        return t;
    }

    private LeaveTypeDto typeDto(LeaveType t) {
        return LeaveTypeDto.builder()
                .id(t.getId())
                .code(t.getCode())
                .maxDaysPerYear(t.getMaxDaysPerYear())
                .build();
    }

    private LeaveRequest req(UUID id, UUID emp, UUID typeId, LocalDate from, LocalDate to, LeaveRequest.Status st) {
        LeaveRequest r = new LeaveRequest();
        r.setId(id);
        r.setEmployeeId(emp);
        r.setTypeId(typeId);
        r.setDateFrom(from);
        r.setDateTo(to);
        r.setStatus(st);
        return r;
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

    // -------- leave types

    @Test
    void countTypes_ok() {
        when(leaveTypeRepository.count()).thenReturn(Mono.just(42L));
        assertEquals(42L, service.countTypes().block());
    }

    @Test
    void listTypes_capsPageSizeAndMaps() {
        Pageable in = PageRequest.of(1, 500, Sort.by("code"));
        LeaveType t = type(UUID.randomUUID(), "VAC", 28);
        when(leaveTypeRepository.findAllBy(any(Pageable.class))).thenReturn(Flux.just(t));
        when(leaveMapper.toDto(t)).thenReturn(typeDto(t));

        List<LeaveTypeDto> out = service.listTypes(in).collectList().block();

        assertEquals(1, out.size());

        // захватываем pageable и убеждаемся, что размер ограничен 50
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveTypeRepository).findAllBy(captor.capture());
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
        when(leaveMapper.toEntity(dto)).thenReturn(toSave);
        when(leaveTypeRepository.save(toSave)).thenReturn(Mono.just(toSave));
        when(leaveMapper.toDto(toSave)).thenReturn(typeDto(toSave));

        LeaveTypeDto out = service.createType(dto).block();
        assertNotNull(out);
        verify(leaveTypeRepository).save(toSave);
    }

    @Test
    void createType_duplicate_precheck() {
        CreateLeaveTypeDto dto = CreateLeaveTypeDto.builder().code("VAC").build();
        when(leaveTypeRepository.existsByCodeIgnoreCase("VAC")).thenReturn(Mono.just(true));

        assertThrows(IllegalStateException.class, () -> service.createType(dto).block());
        verify(leaveTypeRepository, never()).save(any());
    }

    @Test
    void updateType_ok_updatesAndSaves() {
        UUID id = UUID.randomUUID();
        UpdateLeaveTypeDto dto = UpdateLeaveTypeDto.builder().name("Vacation").build();

        LeaveType existing = type(id, "VAC", 28);
        when(leaveTypeRepository.findById(id)).thenReturn(Mono.just(existing));
        doAnswer(inv -> { /* mutate in place as MapStruct would */ return null; })
                .when(leaveMapper).updateEntity(eq(dto), eq(existing));
        when(leaveTypeRepository.save(existing)).thenReturn(Mono.just(existing));
        when(leaveMapper.toDto(existing)).thenReturn(typeDto(existing));

        LeaveTypeDto out = service.updateType(id, dto).block();
        assertNotNull(out);
        verify(leaveTypeRepository).save(existing);
    }

    @Test
    void updateType_notFound_404() {
        when(leaveTypeRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.updateType(UUID.randomUUID(), UpdateLeaveTypeDto.builder().build()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deleteType_ok() {
        UUID id = UUID.randomUUID();
        when(leaveTypeRepository.existsById(id)).thenReturn(Mono.just(true));
        when(leaveTypeRepository.deleteById(id)).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> service.deleteType(id).block());
        verify(leaveTypeRepository).deleteById(id);
    }

    @Test
    void deleteType_notFound_404() {
        when(leaveTypeRepository.existsById((UUID) any())).thenReturn(Mono.just(false));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.deleteType(UUID.randomUUID()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(leaveTypeRepository, never()).deleteById((UUID) any());
    }

    // -------- leave requests: get

    @Test
    void get_ok() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        assertNotNull(service.get(id).block());
    }

    @Test
    void get_notFound_404() {
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.get(UUID.randomUUID()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- create

    @Test
    void create_draft_ok_noLimitCheck() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 1, 10);
        LocalDate to = LocalDate.of(2024, 1, 12);

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(false) // draft
                .build();

        LeaveType type = type(typeId, "VAC", 28);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));

        LeaveRequest entity = req(UUID.randomUUID(), emp, typeId, from, to, LeaveRequest.Status.DRAFT);
        when(leaveMapper.toEntity(dto)).thenReturn(entity);
        when(leaveRequestRepository.save(entity)).thenReturn(Mono.just(entity));
        when(leaveMapper.toDto(entity)).thenReturn(reqDto(entity));

        LeaveRequestDto out = service.create(dto).block();

        assertNotNull(out);
        verify(leaveTypeRepository).findById(typeId);
        verify(leaveRequestRepository).existsOverlaps(emp, from, to);
        // ensureWithinYearLimit не вызывается в draft ветке — мы это косвенно проверяем отсутствием вызова sumApprovedDaysForYear
        verify(leaveRequestRepository, never()).sumApprovedDaysForYear(any(), any(), anyInt());
    }

    @Test
    void create_submit_checksYearLimit_ok() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 2, 1);
        LocalDate to = LocalDate.of(2024, 2, 3); // 3 дня

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(true)
                .build();

        LeaveType type = type(typeId, "VAC", 5); // лимит 5
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(1)); // 1 + 3 <= 5

        LeaveRequest entity = req(UUID.randomUUID(), emp, typeId, from, to, LeaveRequest.Status.DRAFT);
        when(leaveMapper.toEntity(dto)).thenReturn(entity);
        when(leaveRequestRepository.save(entity)).thenReturn(Mono.just(entity));
        when(leaveMapper.toDto(entity)).thenReturn(reqDto(entity));

        assertNotNull(service.create(dto).block());
    }

    @Test
    void create_submit_limitExceeded_throws() {
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 3, 1);
        LocalDate to = LocalDate.of(2024, 3, 5); // 5 дней

        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(emp).typeId(typeId)
                .dateFrom(from).dateTo(to)
                .submit(true)
                .build();

        LeaveType type = type(typeId, "VAC", 7);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.existsOverlaps(emp, from, to)).thenReturn(Mono.just(false));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(4)); // 4 + 5 > 7

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.create(dto).block());
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

        assertThrows(IllegalStateException.class, () -> service.create(dto).block());
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

        lenient().when(leaveRequestRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(dto).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void create_badDateOrder_throwsImmediately() {
        CreateLeaveRequestDto dto = CreateLeaveRequestDto.builder()
                .employeeId(UUID.randomUUID())
                .typeId(UUID.randomUUID())
                .dateFrom(LocalDate.of(2024, 5, 10))
                .dateTo(LocalDate.of(2024, 5, 1)) // to < from
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.create(dto).block());
        verifyNoInteractions(leaveTypeRepository, leaveRequestRepository);
    }

    // -------- update

    @Test
    void update_ok_fromPending_validatesAndSaves() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        LeaveRequest existing = req(id, emp, typeId,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 3), LeaveRequest.Status.PENDING);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        UpdateLeaveRequestDto dto = UpdateLeaveRequestDto.builder()
                .dateTo(LocalDate.of(2024, 4, 4)) // расширим на день
                .comment("upd")
                .build();

        doAnswer(inv -> { // применим изменения как mapstruct
            LeaveRequest target = inv.getArgument(1);
            target.setDateTo(LocalDate.of(2024, 4, 4));
            target.setComment("upd");
            return null;
        }).when(leaveMapper).updateEntity(eq(dto), eq(existing));

        when(leaveRequestRepository.existsOverlapsExcluding(id, emp,
                LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 4)))
                .thenReturn(Mono.just(false));

        when(leaveRequestRepository.save(existing)).thenReturn(Mono.just(existing));
        when(leaveMapper.toDto(existing)).thenReturn(reqDto(existing));

        LeaveRequestDto out = service.update(id, dto).block();

        assertNotNull(out);
        assertEquals(LocalDate.of(2024, 4, 4), existing.getDateTo());
        assertEquals("upd", existing.getComment());
    }

    @Test
    void update_statusNotAllowed_throws() {
        UUID id = UUID.randomUUID();
        LeaveRequest existing = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.APPROVED);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.update(id, UpdateLeaveRequestDto.builder().build()).block());
        assertTrue(ex.getMessage().contains("only DRAFT/PENDING"));
    }

    @Test
    void update_overlapsExcluding_throws() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        LeaveRequest existing = req(id, emp, UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);

        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(existing));

        doAnswer(inv -> null).when(leaveMapper).updateEntity(any(UpdateLeaveRequestDto.class), eq(existing));

        when(leaveRequestRepository.existsOverlapsExcluding(
                any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Mono.just(true));

        when(leaveRequestRepository.save(existing)).thenReturn(Mono.just(existing));

        StepVerifier.create(service.update(id, UpdateLeaveRequestDto.builder().build()))
                .expectErrorSatisfies(err -> assertTrue(err instanceof IllegalStateException))
                .verify();

        verify(leaveMapper, never()).toDto((LeaveType) any());
        verify(leaveRequestRepository).existsOverlapsExcluding(any(), any(), any(), any());
    }

    @Test
    void update_notFound_404() {
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.update(UUID.randomUUID(), UpdateLeaveRequestDto.builder().build()).block());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    // -------- approve / reject / cancel

    @Test
    void approve_ok_checksLimit_andSavesApproved() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate from = LocalDate.of(2024, 6, 10);
        LocalDate to = LocalDate.of(2024, 6, 12); // 3 дня

        LeaveRequest e = req(id, emp, typeId, from, to, LeaveRequest.Status.PENDING);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        LeaveType type = type(typeId, "VAC", 10);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(6)); // 6+3<=10

        when(leaveRequestRepository.save(e)).thenReturn(Mono.just(e));
        when(leaveMapper.toDto(e)).thenAnswer(inv -> reqDto(e));

        DecisionDto d = DecisionDto.builder().approverId(UUID.randomUUID()).comment("ok").build();

        LeaveRequestDto out = service.approve(id, d).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.APPROVED, e.getStatus());
        assertEquals(d.getApproverId(), e.getApproverId());
        assertEquals("ok", e.getComment());
    }

    @Test
    void approve_notPending_throws() {
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.just(e));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.approve(UUID.randomUUID(), DecisionDto.builder().build()).block());
        assertTrue(ex.getMessage().contains("not PENDING"));
    }

    @Test
    void approve_limitExceeded_throws() {
        UUID id = UUID.randomUUID();
        UUID emp = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();

        LeaveRequest e = req(id, emp, typeId,
                LocalDate.of(2024,7,1), LocalDate.of(2024,7,4), LeaveRequest.Status.PENDING); // 4 дня
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));

        LeaveType type = type(typeId, "VAC", 7);
        when(leaveTypeRepository.findById(typeId)).thenReturn(Mono.just(type));
        when(leaveRequestRepository.sumApprovedDaysForYear(emp, typeId, 2024)).thenReturn(Mono.just(4)); // 4+4>7

        assertThrows(IllegalStateException.class, () -> service.approve(id, DecisionDto.builder().build()).block());
        verify(leaveRequestRepository, never()).save(any());
    }

    @Test
    void reject_ok_setsRejected() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.PENDING);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));
        when(leaveRequestRepository.save(e)).thenReturn(Mono.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        DecisionDto d = DecisionDto.builder().approverId(UUID.randomUUID()).comment("nope").build();
        LeaveRequestDto out = service.reject(id, d).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.REJECTED, e.getStatus());
        assertEquals("nope", e.getComment());
        assertEquals(d.getApproverId(), e.getApproverId());
    }

    @Test
    void cancel_ok_fromApproved() {
        UUID id = UUID.randomUUID();
        LeaveRequest e = req(id, UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.APPROVED);
        when(leaveRequestRepository.findById(id)).thenReturn(Mono.just(e));
        when(leaveRequestRepository.save(e)).thenReturn(Mono.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        LeaveRequestDto out = service.cancel(id).block();

        assertNotNull(out);
        assertEquals(LeaveRequest.Status.CANCELED, e.getStatus());
    }

    @Test
    void cancel_badStatus_throws() {
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(1), LeaveRequest.Status.DRAFT);
        when(leaveRequestRepository.findById((UUID) any())).thenReturn(Mono.just(e));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.cancel(e.getId()).block());
        assertTrue(ex.getMessage().contains("only PENDING/APPROVED"));
    }

    // -------- lists & counts

    @Test
    void countLeaveByEmployee_ok() {
        UUID emp = UUID.randomUUID();
        when(leaveRequestRepository.countByEmployeeId(emp)).thenReturn(Mono.just(7L));
        assertEquals(7L, service.countLeaveByEmployee(emp).block());
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

        List<LeaveRequestDto> list = service.listByEmployee(emp, p).collectList().block();

        assertEquals(1, list.size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveRequestRepository).findByEmployeeIdOrderByDateFromDesc(eq(emp), captor.capture());
        assertEquals(p, captor.getValue());
    }

    @Test
    void countLeaveByStatus_ok() {
        when(leaveRequestRepository.countByStatus(LeaveRequest.Status.PENDING)).thenReturn(Mono.just(3L));
        assertEquals(3L, service.countLeaveByStatus(LeaveRequest.Status.PENDING).block());
    }

    @Test
    void listByStatus_mapsAndKeepsPageable() {
        Pageable p = PageRequest.of(1, 5, Sort.by("dateFrom"));
        LeaveRequest e = req(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), LocalDate.now().plusDays(2), LeaveRequest.Status.PENDING);

        when(leaveRequestRepository.findByStatus(eq(LeaveRequest.Status.PENDING), any(Pageable.class)))
                .thenReturn(Flux.just(e));
        when(leaveMapper.toDto(e)).thenReturn(reqDto(e));

        List<LeaveRequestDto> list = service.listByStatus(LeaveRequest.Status.PENDING, p).collectList().block();

        assertEquals(1, list.size());
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(leaveRequestRepository).findByStatus(eq(LeaveRequest.Status.PENDING), captor.capture());
        assertEquals(p, captor.getValue());
    }
}
