package temp.unipeople.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import temp.unipeople.feature.employment.dto.*;
import temp.unipeople.feature.employment.entity.Employment;
import temp.unipeople.feature.employment.mapper.EmploymentMapper;
import temp.unipeople.feature.employment.repository.EmploymentRepository;
import temp.unipeople.feature.employment.service.EmploymentService;

@ExtendWith(MockitoExtension.class)
class EmploymentServiceTest {

  @Mock EmploymentRepository repo;
  @Mock EmploymentMapper mapper;

  EmploymentService service;

  @BeforeEach
  void setUp() {
    service = new EmploymentService(repo, mapper);
  }

  @Test
  void get_ok() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    when(repo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    assertNotNull(service.get(id));
  }

  @Test
  void get_notFound() {
    when(repo.findById(any())).thenReturn(Optional.empty());
    assertThrows(EntityNotFoundException.class, () -> service.get(UUID.randomUUID()));
  }

  @Test
  void create_throws_whenOverlapsFound() {
    var dto =
        CreateEmploymentDto.builder()
            .employeeId(UUID.randomUUID())
            .departmentId(UUID.randomUUID())
            .positionId(UUID.randomUUID())
            .startDate(LocalDate.of(2024, 1, 1))
            .build();

    when(repo.findOverlaps(any(), any(), any(), any(), isNull()))
        .thenReturn(List.of(new Employment()));

    assertThrows(IllegalStateException.class, () -> service.create(dto));
    verify(repo, never()).save(any());
  }

  @Test
  void create_setsDefaultRateAndSaves() {
    var dto =
        CreateEmploymentDto.builder()
            .employeeId(UUID.randomUUID())
            .departmentId(UUID.randomUUID())
            .positionId(UUID.randomUUID())
            .startDate(LocalDate.of(2024, 1, 1))
            .build();

    Employment entity = new Employment();
    Employment saved = new Employment();

    when(repo.findOverlaps(any(), any(), any(), any(), isNull()))
        .thenReturn(Collections.emptyList());
    when(mapper.toEntity(dto)).thenReturn(entity);
    when(repo.save(entity)).thenReturn(saved);
    when(mapper.toDto(saved)).thenReturn(EmploymentDto.builder().build());

    EmploymentDto result = service.create(dto);

    assertNotNull(result);
    assertEquals(BigDecimal.valueOf(1.00), entity.getRate());
    verify(repo).save(entity);
  }

  @Test
  void update_ok() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    e.setStartDate(LocalDate.of(2024, 1, 1));
    when(repo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    EmploymentDto res = service.update(id, UpdateEmploymentDto.builder().build());

    assertNotNull(res);
    verify(mapper).updateEntity(any(UpdateEmploymentDto.class), eq(e));
  }

  @Test
  void update_notFound() {
    when(repo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.update(UUID.randomUUID(), UpdateEmploymentDto.builder().build()));
  }

  @Test
  void update_throws_whenEndBeforeStart() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    e.setStartDate(LocalDate.of(2024, 1, 10));
    e.setEndDate(LocalDate.of(2024, 1, 5));
    when(repo.findById(id)).thenReturn(Optional.of(e));
    doAnswer(
            inv -> {
              Employment target = inv.getArgument(1);
              target.setEndDate(LocalDate.of(2024, 1, 5));
              return null;
            })
        .when(mapper)
        .updateEntity(any(UpdateEmploymentDto.class), eq(e));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.update(
                id, UpdateEmploymentDto.builder().endDate(LocalDate.of(2024, 1, 5)).build()));
  }

  @Test
  void close_returnsImmediately_whenAlreadyClosed() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    e.setStatus(Employment.Status.CLOSED);
    when(repo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    assertNotNull(service.close(id, CloseEmploymentDto.builder().build()));
    verify(mapper).toDto(e);
  }

  @Test
  void close_setsEndAndStatus_whenBodyHasEndDate() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    e.setStartDate(LocalDate.of(2024, 1, 1));
    e.setStatus(Employment.Status.ACTIVE);
    when(repo.findById(id)).thenReturn(Optional.of(e));
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    LocalDate end = LocalDate.of(2024, 2, 1);
    EmploymentDto res = service.close(id, CloseEmploymentDto.builder().endDate(end).build());

    assertNotNull(res);
    assertEquals(end, e.getEndDate());
    assertEquals(Employment.Status.CLOSED, e.getStatus());
  }

  @Test
  void close_throws_whenEndBeforeStart() {
    UUID id = UUID.randomUUID();
    Employment e = new Employment();
    e.setStartDate(LocalDate.of(2024, 5, 1));
    e.setStatus(Employment.Status.ACTIVE);
    when(repo.findById(id)).thenReturn(Optional.of(e));

    LocalDate badEnd = LocalDate.of(2024, 4, 30);
    assertThrows(
        IllegalArgumentException.class,
        () -> service.close(id, CloseEmploymentDto.builder().endDate(badEnd).build()));
  }

  @Test
  void close_notFound() {
    when(repo.findById(any())).thenReturn(Optional.empty());
    assertThrows(
        EntityNotFoundException.class,
        () -> service.close(UUID.randomUUID(), CloseEmploymentDto.builder().build()));
  }

  @Test
  void listByEmployee_sortsDescAndMaps() {
    UUID empId = UUID.randomUUID();
    Pageable in = PageRequest.of(0, 10);
    Employment e = new Employment();
    Page<Employment> page = new PageImpl<>(List.of(e), in, 1);

    when(repo.findByEmployeeIdOrderByStartDateDesc(eq(empId), any())).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    var res = service.listByEmployee(empId, in);
    assertEquals(1, res.getTotalElements());
    verify(repo).findByEmployeeIdOrderByStartDateDesc(eq(empId), eq(in));
  }

  @Test
  void listByDepartment_activeTrue_usesActiveQuery() {
    UUID depId = UUID.randomUUID();
    Pageable in = PageRequest.of(0, 5);
    Employment e = new Employment();
    Page<Employment> page = new PageImpl<>(List.of(e), in, 1);

    when(repo.findByDepartmentIdAndStatus(depId, Employment.Status.ACTIVE, in)).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    var res = service.listByDepartment(depId, true, in);
    assertEquals(1, res.getTotalElements());
    verify(repo).findByDepartmentIdAndStatus(depId, Employment.Status.ACTIVE, in);
  }

  @Test
  void listByDepartment_activeFalse_usesAllQuery() {
    UUID depId = UUID.randomUUID();
    Pageable in = PageRequest.of(0, 5);
    Employment e = new Employment();
    Page<Employment> page = new PageImpl<>(List.of(e), in, 1);

    when(repo.findByDepartmentId(depId, in)).thenReturn(page);
    when(mapper.toDto(e)).thenReturn(EmploymentDto.builder().build());

    var res = service.listByDepartment(depId, false, in);
    assertEquals(1, res.getTotalElements());
    verify(repo).findByDepartmentId(depId, in);
  }
}
