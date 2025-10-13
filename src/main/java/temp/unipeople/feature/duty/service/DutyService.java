package temp.unipeople.feature.duty.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class DutyService {

  private final DutyRepository dutyRepository;
  private final DutyAssignmentRepository dutyAssignmentRepository;
  private final DutyMapper dutyMapper;
  private final DutyAssignmentMapper dutyAssignmentMapper;

  @Transactional(readOnly = true)
  public Page<DutyDto> findAll(Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "code"));
    return dutyRepository.findAll(sorted).map(dutyMapper::toDto);
  }

  @Transactional(readOnly = true)
  public DutyDto get(UUID id) {
    Duty e =
        dutyRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("duty not found: " + id));
    return dutyMapper.toDto(e);
  }

  @Transactional
  public DutyDto create(CreateDutyDto dto) {
    if (dutyRepository.existsByCodeIgnoreCase(dto.getCode())) {
      throw new IllegalStateException("duty code already exists");
    }
    Duty e = dutyMapper.toEntity(dto);
    try {
      e = dutyRepository.saveAndFlush(e);
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException("duty code already exists", ex);
    }
    return dutyMapper.toDto(e);
  }

  @Transactional
  public DutyDto update(UUID id, UpdateDutyDto dto) {
    Duty e =
        dutyRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("duty not found: " + id));
    dutyMapper.updateEntity(dto, e);
    try {
      dutyRepository.flush();
    } catch (DataIntegrityViolationException ex) {
      throw new IllegalStateException("duty code already exists", ex);
    }
    return dutyMapper.toDto(e);
  }

  @Transactional
  public void delete(UUID id) {
    if (!dutyRepository.existsById(id)) {
      throw new EntityNotFoundException("duty not found: " + id);
    }
    dutyRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public Page<DutyAssignmentDto> listAssignments(UUID dutyId, Pageable pageable) {
    Pageable sorted =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "assigned_at"));
    Page<DepartmentDutyAssignment> page = dutyAssignmentRepository.findByDutyId(dutyId, sorted);
    return page.map(dutyAssignmentMapper::toDto);
  }
}
