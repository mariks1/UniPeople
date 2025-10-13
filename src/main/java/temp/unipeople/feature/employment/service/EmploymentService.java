package temp.unipeople.feature.employment.service;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.employment.dto.CloseEmploymentDto;
import temp.unipeople.feature.employment.dto.CreateEmploymentDto;
import temp.unipeople.feature.employment.dto.EmploymentDto;
import temp.unipeople.feature.employment.dto.UpdateEmploymentDto;
import temp.unipeople.feature.employment.entity.Employment;
import temp.unipeople.feature.employment.mapper.EmploymentMapper;
import temp.unipeople.feature.employment.repository.EmploymentRepository;

@Service
@RequiredArgsConstructor
public class EmploymentService {

  private final EmploymentRepository repo;
  private final EmploymentMapper mapper;

  public EmploymentDto get(UUID id) {
    var e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employment not found: " + id));
    return mapper.toDto(e);
  }

  @Transactional
  public EmploymentDto create(CreateEmploymentDto dto) {
    var overlaps =
        repo.findOverlaps(
            dto.getEmployeeId(),
            dto.getDepartmentId(),
            dto.getPositionId(),
            dto.getStartDate(),
            null);
    if (!overlaps.isEmpty()) {
      throw new IllegalStateException(
          "overlapping employment exists for employee/department/position");
    }

    var e = mapper.toEntity(dto);
    if (e.getRate() == null) e.setRate(java.math.BigDecimal.valueOf(1.00));
    e = repo.save(e);
    return mapper.toDto(e);
  }

  @Transactional
  public EmploymentDto update(UUID id, UpdateEmploymentDto dto) {
    var e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employment not found: " + id));

    mapper.updateEntity(dto, e);

    if (e.getEndDate() != null && e.getEndDate().isBefore(e.getStartDate())) {
      throw new IllegalArgumentException("endDate must be on or after startDate");
    }
    return mapper.toDto(e);
  }

  @Transactional
  public EmploymentDto close(UUID id, CloseEmploymentDto body) {
    var e =
        repo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("employment not found: " + id));
    if (Employment.Status.CLOSED.equals(e.getStatus())) {
      return mapper.toDto(e);
    }
    var end = body != null && body.getEndDate() != null ? body.getEndDate() : LocalDate.now();
    if (end.isBefore(e.getStartDate())) {
      throw new IllegalArgumentException("endDate must be on or after startDate");
    }
    e.setEndDate(end);
    e.setStatus(Employment.Status.CLOSED);
    return mapper.toDto(e);
  }

  public Page<EmploymentDto> listByEmployee(UUID employeeId, Pageable pageable) {
    var page = repo.findByEmployeeIdOrderByStartDateDesc(employeeId, pageable);
    return page.map(mapper::toDto);
  }

  public Page<EmploymentDto> listByDepartment(
      UUID departmentId, boolean active, Pageable pageable) {
    Page<Employment> page =
        active
            ? repo.findByDepartmentIdAndStatus(departmentId, Employment.Status.ACTIVE, pageable)
            : repo.findByDepartmentId(departmentId, pageable);
    return page.map(mapper::toDto);
  }
}
