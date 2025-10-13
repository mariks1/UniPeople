package temp.unipeople.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import temp.unipeople.feature.leave.dto.*;
import temp.unipeople.feature.leave.entity.LeaveRequest;
import temp.unipeople.feature.leave.entity.LeaveType;
import temp.unipeople.feature.leave.mapper.LeaveMapper;
import temp.unipeople.feature.leave.repository.LeaveRequestRepository;
import temp.unipeople.feature.leave.repository.LeaveTypeRepository;
import temp.unipeople.feature.leave.service.LeaveService;

@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

  @Mock LeaveTypeRepository leaveTypeRepo;
  @Mock LeaveRequestRepository leaveReqRepo;
  @Mock LeaveMapper mapper;

  LeaveService service;

  @BeforeEach
  void setUp() {
    service = new LeaveService(leaveTypeRepo, leaveReqRepo, mapper);
  }

  @Test
  void listTypes_mapsPage() {
    Pageable p = PageRequest.of(0, 10);
    LeaveType e = new LeaveType();
    Page<LeaveType> page = new PageImpl<>(List.of(e), p, 1);
    when(leaveTypeRepo.findAll(p)).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(LeaveTypeDto.builder().build());

    var res = service.listTypes(p);

    assertEquals(1, res.getTotalElements());
    verify(leaveTypeRepo).findAll(p);
  }

  @Test
  void createType_ok() {
    var dto = CreateLeaveTypeDto.builder().code("VAC").name("Vacation").build();
    when(leaveTypeRepo.existsByCodeIgnoreCase("VAC")).thenReturn(false);
    LeaveType entity = new LeaveType();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(leaveTypeRepo.saveAndFlush(entity)).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(LeaveTypeDto.builder().build());

    assertNotNull(service.createType(dto));
  }

  @Test
  void createType_duplicate_throws() {
    var dto = CreateLeaveTypeDto.builder().code("VAC").name("Vacation").build();
    when(leaveTypeRepo.existsByCodeIgnoreCase("VAC")).thenReturn(true);
    assertThrows(IllegalStateException.class, () -> service.createType(dto));
  }

  @Test
  void updateType_ok() {
    UUID id = UUID.randomUUID();
    LeaveType e = new LeaveType();
    when(leaveTypeRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(LeaveTypeDto.builder().build());

    var dto = UpdateLeaveTypeDto.builder().name("N").build();
    assertNotNull(service.updateType(id, dto));
    verify(mapper).updateEntity(dto, e);
    verify(leaveTypeRepo).flush();
  }

  @Test
  void updateType_notFound() {
    when(leaveTypeRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.updateType(UUID.randomUUID(), UpdateLeaveTypeDto.builder().build()));
  }

  @Test
  void deleteType_ok() {
    UUID id = UUID.randomUUID();
    when(leaveTypeRepo.existsById(id)).thenReturn(true);
    service.deleteType(id);
    verify(leaveTypeRepo).deleteById(id);
  }

  @Test
  void deleteType_notFound() {
    when(leaveTypeRepo.existsById(any())).thenReturn(false);
    assertThrows(EntityNotFoundException.class, () -> service.deleteType(UUID.randomUUID()));
  }

  @Test
  void get_ok() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    assertNotNull(service.get(id));
  }

  @Test
  void get_notFound() {
    when(leaveReqRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
  }

  @Test
  void create_throws_whenDateToBeforeDateFrom() {
    var dto =
        CreateLeaveRequestDto.builder()
            .employeeId(UUID.randomUUID())
            .typeId(UUID.randomUUID())
            .dateFrom(LocalDate.of(2025, 1, 10))
            .dateTo(LocalDate.of(2025, 1, 5))
            .submit(true)
            .build();
    assertThrows(IllegalArgumentException.class, () -> service.create(dto));
  }

  @Test
  void create_throws_whenTypeNotFound() {
    var dto = baseCreateDto();
    when(leaveTypeRepo.findById(dto.getTypeId())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.create(dto));
  }

  @Test
  void create_throws_whenOverlapsExist() {
    var dto = baseCreateDto();
    when(leaveTypeRepo.findById(dto.getTypeId())).thenReturn(Optional.of(new LeaveType()));
    when(leaveReqRepo.findOverlaps(dto.getEmployeeId(), dto.getDateFrom(), dto.getDateTo()))
        .thenReturn(List.of(new LeaveRequest()));
    assertThrows(IllegalStateException.class, () -> service.create(dto));
  }

  @Test
  void create_throws_whenYearlyLimitExceeded_onSubmit() {
    var dto = baseCreateDto(true);
    LeaveType type = new LeaveType();
    type.setMaxDaysPerYear(5);
    when(leaveTypeRepo.findById(dto.getTypeId())).thenReturn(Optional.of(type));
    when(leaveReqRepo.findOverlaps(any(), any(), any())).thenReturn(Collections.emptyList());
    when(leaveTypeRepo.getReferenceById(dto.getTypeId())).thenReturn(type);
    when(leaveReqRepo.sumApprovedDaysForYear(
            dto.getEmployeeId(), dto.getTypeId(), dto.getDateFrom().getYear()))
        .thenReturn(4);

    assertThrows(IllegalStateException.class, () -> service.create(dto));
  }

  @Test
  void create_ok_withinLimit_orNoLimit() {
    var dto = baseCreateDto(true);
    LeaveType type = new LeaveType();
    type.setMaxDaysPerYear(10);

    when(leaveTypeRepo.findById(dto.getTypeId())).thenReturn(Optional.of(type));
    when(leaveReqRepo.findOverlaps(any(), any(), any())).thenReturn(Collections.emptyList());
    when(leaveTypeRepo.getReferenceById(dto.getTypeId())).thenReturn(type);
    when(leaveReqRepo.sumApprovedDaysForYear(any(), any(), anyInt())).thenReturn(2);

    LeaveRequest entity = new LeaveRequest();
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(leaveReqRepo.save(entity)).thenReturn(entity);
    when(mapper.toDto(entity)).thenReturn(LeaveRequestDto.builder().build());

    assertNotNull(service.create(dto));
  }

  @Test
  void update_notFound() {
    when(leaveReqRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.update(UUID.randomUUID(), UpdateLeaveRequestDto.builder().build()));
  }

  @Test
  void update_throws_whenStatusNotDraftOrPending() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.APPROVED);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    assertThrows(
        IllegalStateException.class,
        () -> service.update(id, UpdateLeaveRequestDto.builder().build()));
  }

  @Test
  void update_throws_whenDateToBeforeDateFrom_afterMapper() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.DRAFT);
    e.setDateFrom(LocalDate.of(2025, 1, 10));
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    doAnswer(
            inv -> {
              LeaveRequest target = inv.getArgument(1);
              target.setDateTo(LocalDate.of(2025, 1, 5));
              return null;
            })
        .when(mapper)
        .updateEntity(any(UpdateLeaveRequestDto.class), eq(e));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.update(
                id, UpdateLeaveRequestDto.builder().dateTo(LocalDate.of(2025, 1, 5)).build()));
  }

  @Test
  void update_throws_whenOverlapsOtherRequests() {
    UUID id = UUID.randomUUID();
    LeaveRequest self = new LeaveRequest();
    self.setId(id);
    self.setEmployeeId(UUID.randomUUID());
    self.setStatus(LeaveRequest.Status.PENDING);
    self.setDateFrom(LocalDate.of(2025, 1, 1));
    self.setDateTo(LocalDate.of(2025, 1, 3));
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(self));
    doAnswer(inv -> null).when(mapper).updateEntity(any(UpdateLeaveRequestDto.class), eq(self));

    LeaveRequest other = new LeaveRequest();
    other.setId(UUID.randomUUID());
    when(leaveReqRepo.findOverlaps(self.getEmployeeId(), self.getDateFrom(), self.getDateTo()))
        .thenReturn(List.of(other));

    assertThrows(
        IllegalStateException.class,
        () -> service.update(id, UpdateLeaveRequestDto.builder().build()));
  }

  @Test
  void update_ok() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.DRAFT);
    e.setDateFrom(LocalDate.of(2025, 1, 1));
    e.setDateTo(LocalDate.of(2025, 1, 3));
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    when(leaveReqRepo.findOverlaps(any(), any(), any())).thenReturn(Collections.emptyList());
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    assertNotNull(service.update(id, UpdateLeaveRequestDto.builder().build()));
  }

  @Test
  void approve_notFound() {
    when(leaveReqRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.approve(UUID.randomUUID(), DecisionDto.builder().build()));
  }

  @Test
  void approve_wrongStatus() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.DRAFT);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    assertThrows(
        IllegalStateException.class, () -> service.approve(id, DecisionDto.builder().build()));
  }

  @Test
  void approve_throws_whenLimitExceeded() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.PENDING);
    e.setEmployeeId(UUID.randomUUID());
    e.setTypeId(UUID.randomUUID());
    e.setDateFrom(LocalDate.of(2025, 1, 1));
    e.setDateTo(LocalDate.of(2025, 1, 5));
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));

    LeaveType type = new LeaveType();
    type.setMaxDaysPerYear(3);
    when(leaveTypeRepo.getReferenceById(e.getTypeId())).thenReturn(type);
    when(leaveReqRepo.sumApprovedDaysForYear(any(), any(), anyInt())).thenReturn(1);

    assertThrows(
        IllegalStateException.class,
        () -> service.approve(id, DecisionDto.builder().approverId(UUID.randomUUID()).build()));
  }

  @Test
  void approve_ok_setsApproverCommentAndStatus() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.PENDING);
    e.setEmployeeId(UUID.randomUUID());
    e.setTypeId(UUID.randomUUID());
    e.setDateFrom(LocalDate.of(2025, 1, 1));
    e.setDateTo(LocalDate.of(2025, 1, 2));
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));

    LeaveType type = new LeaveType();
    type.setMaxDaysPerYear(10);
    when(leaveTypeRepo.getReferenceById(e.getTypeId())).thenReturn(type);
    when(leaveReqRepo.sumApprovedDaysForYear(any(), any(), anyInt())).thenReturn(0);

    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    var dto =
        service.approve(
            id, DecisionDto.builder().approverId(UUID.randomUUID()).comment("ok").build());

    assertNotNull(dto);
    assertEquals(LeaveRequest.Status.APPROVED, e.getStatus());
    assertEquals("ok", e.getComment());
  }

  @Test
  void reject_wrongStatus() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.DRAFT);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    assertThrows(
        IllegalStateException.class, () -> service.reject(id, DecisionDto.builder().build()));
  }

  @Test
  void reject_ok_setsFields() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.PENDING);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    var res = service.reject(id, DecisionDto.builder().approverId(UUID.randomUUID()).build());
    assertNotNull(res);
    assertEquals(LeaveRequest.Status.REJECTED, e.getStatus());
  }

  @Test
  void cancel_notFound() {
    when(leaveReqRepo.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.cancel(UUID.randomUUID()));
  }

  @Test
  void cancel_ok_whenPendingOrApproved() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.APPROVED);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    assertNotNull(service.cancel(id));
    assertEquals(LeaveRequest.Status.CANCELED, e.getStatus());
  }

  @Test
  void cancel_throws_whenOtherStatus() {
    UUID id = UUID.randomUUID();
    LeaveRequest e = new LeaveRequest();
    e.setStatus(LeaveRequest.Status.DRAFT);
    when(leaveReqRepo.findById(id)).thenReturn(Optional.of(e));
    assertThrows(IllegalStateException.class, () -> service.cancel(id));
  }

  @Test
  void listByEmployee_maps() {
    UUID empId = UUID.randomUUID();
    Pageable p = PageRequest.of(0, 5);
    LeaveRequest e = new LeaveRequest();
    Page<LeaveRequest> page = new PageImpl<>(List.of(e), p, 1);
    when(leaveReqRepo.findByEmployeeId(empId, p)).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    var res = service.listByEmployee(empId, p);
    assertEquals(1, res.getTotalElements());
  }

  @Test
  void listByStatus_maps() {
    Pageable p = PageRequest.of(0, 5);
    LeaveRequest e = new LeaveRequest();
    Page<LeaveRequest> page = new PageImpl<>(List.of(e), p, 1);
    when(leaveReqRepo.findByStatus(LeaveRequest.Status.PENDING, p)).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(LeaveRequestDto.builder().build());

    var res = service.listByStatus(LeaveRequest.Status.PENDING, p);
    assertEquals(1, res.getTotalElements());
  }

  private CreateLeaveRequestDto baseCreateDto() {
    return baseCreateDto(false);
  }

  private CreateLeaveRequestDto baseCreateDto(boolean submit) {
    return CreateLeaveRequestDto.builder()
        .employeeId(UUID.randomUUID())
        .typeId(UUID.randomUUID())
        .dateFrom(LocalDate.of(2025, 1, 1))
        .dateTo(LocalDate.of(2025, 1, 3))
        .submit(submit)
        .build();
  }
}
