package temp.unipeople;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import temp.unipeople.feature.duty.dto.CreateDutyDto;
import temp.unipeople.feature.duty.dto.DutyAssignmentDto;
import temp.unipeople.feature.duty.dto.DutyDto;
import temp.unipeople.feature.duty.dto.UpdateDutyDto;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;
import temp.unipeople.feature.duty.entity.Duty;
import temp.unipeople.feature.duty.mapper.DutyAssignmentMapper;
import temp.unipeople.feature.duty.mapper.DutyMapper;
import temp.unipeople.feature.duty.repository.DutyAssignmentRepository;
import temp.unipeople.feature.duty.repository.DutyRepository;
import temp.unipeople.feature.duty.service.DutyService;

@ExtendWith(MockitoExtension.class)
class DutyServiceTest {
  @Mock DutyRepository dutyRepo;
  @Mock DutyAssignmentRepository assignRepo;
  @Mock DutyMapper dutyMapper;
  @Mock DutyAssignmentMapper assignMapper;

  DutyService service;

  @BeforeEach
  void setUp() {
    service = new DutyService(dutyRepo, assignRepo, dutyMapper, assignMapper);
  }

  // ---- findAll ----
  @Test
  void findAll_appliesDefaultSortWhenNone() {
    Pageable in = PageRequest.of(0, 10); // без сортировки
    when(dutyRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());

    service.findAll(in);

    var captor = ArgumentCaptor.forClass(Pageable.class);
    verify(dutyRepo).findAll(captor.capture());
    Sort.Order order = captor.getValue().getSort().getOrderFor("code");
    assertNotNull(order);
    assertEquals(Sort.Direction.ASC, order.getDirection());
  }

  @Test
  void findAll_keepsProvidedSort() {
    Pageable in = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));
    when(dutyRepo.findAll(in)).thenReturn(Page.empty());

    service.findAll(in);

    verify(dutyRepo).findAll(in); // передан ровно тот же pageable
  }

  // ---- get ----
  @Test
  void get_ok() {
    UUID id = UUID.randomUUID();
    Duty e = new Duty();
    when(dutyRepo.findById(id)).thenReturn(java.util.Optional.of(e));
    when(dutyMapper.toDto(e)).thenReturn(DutyDto.builder().build());

    assertNotNull(service.get(id));
  }

  @Test
  void get_notFound() {
    UUID id = UUID.randomUUID();
    when(dutyRepo.findById(id)).thenReturn(java.util.Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(id));
  }

  // ---- create ----
  @Test
  void create_wrapsDataIntegrityViolation() {
    var dto = CreateDutyDto.builder().code("X1").name("n").build();
    Duty e = new Duty();
    when(dutyRepo.existsByCodeIgnoreCase("X1")).thenReturn(false);
    when(dutyMapper.toEntity(dto)).thenReturn(e);
    when(dutyRepo.saveAndFlush(e)).thenThrow(new DataIntegrityViolationException("dup"));
    assertThrows(IllegalStateException.class, () -> service.create(dto));
  }

  // твои create_ok и create_throws_whenCodeExists остаются

  // ---- update ----
  @Test
  void update_ok() {
    UUID id = UUID.randomUUID();
    Duty e = new Duty();
    when(dutyRepo.findById(id)).thenReturn(java.util.Optional.of(e));
    when(dutyMapper.toDto(e)).thenReturn(DutyDto.builder().build());

    var res = service.update(id, UpdateDutyDto.builder().build());
    assertNotNull(res);
    verify(dutyMapper).updateEntity(any(UpdateDutyDto.class), eq(e));
    verify(dutyRepo).flush();
  }

  @Test
  void update_notFound() {
    when(dutyRepo.findById(any())).thenReturn(java.util.Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.update(UUID.randomUUID(), UpdateDutyDto.builder().build()));
  }

  @Test
  void update_wrapsDataIntegrityViolation() {
    UUID id = UUID.randomUUID();
    Duty e = new Duty();
    when(dutyRepo.findById(id)).thenReturn(java.util.Optional.of(e));
    doThrow(new DataIntegrityViolationException("dup")).when(dutyRepo).flush();

    assertThrows(
        IllegalStateException.class, () -> service.update(id, UpdateDutyDto.builder().build()));
  }

  // ---- delete ----
  @Test
  void delete_notFound() {
    when(dutyRepo.existsById(any())).thenReturn(false);
    assertThrows(EntityNotFoundException.class, () -> service.delete(UUID.randomUUID()));
  }

  @Test
  void delete_ok() {
    UUID id = UUID.randomUUID();
    when(dutyRepo.existsById(id)).thenReturn(true);

    service.delete(id);

    verify(dutyRepo).deleteById(id);
  }

  // ---- listAssignments ----
  @Test
  void listAssignments_appliesDefaultSortAndMaps() {
    UUID dutyId = UUID.randomUUID();
    Pageable in = PageRequest.of(0, 5); // без сортировки
    var entity = DepartmentDutyAssignment.builder().build();
    var page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(entity), in, 1);

    when(assignRepo.findByDutyId(any(), any())).thenReturn(page);
    when(assignMapper.toDto(entity)).thenReturn(DutyAssignmentDto.builder().build());

    var result = service.listAssignments(dutyId, in);
    assertEquals(1, result.getTotalElements());

    var captor = ArgumentCaptor.forClass(Pageable.class);
    verify(assignRepo).findByDutyId(eq(dutyId), captor.capture());
    Sort.Order order = captor.getValue().getSort().getOrderFor("assignedAt");
    assertNotNull(order);
    assertEquals(Sort.Direction.DESC, order.getDirection());
  }
}
