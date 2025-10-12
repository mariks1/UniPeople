package temp.unipeople.feature.leave.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import temp.unipeople.feature.leave.dto.CreateLeaveRequestDto;
import temp.unipeople.feature.leave.dto.CreateLeaveTypeDto;
import temp.unipeople.feature.leave.dto.DecisionDto;
import temp.unipeople.feature.leave.dto.LeaveRequestDto;
import temp.unipeople.feature.leave.dto.LeaveTypeDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveRequestDto;
import temp.unipeople.feature.leave.dto.UpdateLeaveTypeDto;
import temp.unipeople.feature.leave.entity.LeaveRequest;
import temp.unipeople.feature.leave.mapper.LeaveMapper;
import temp.unipeople.feature.leave.repository.LeaveRequestRepository;
import temp.unipeople.feature.leave.repository.LeaveTypeRepository;

@Service
@RequiredArgsConstructor
public class LeaveService {

  private final LeaveTypeRepository leaveTypeRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveMapper leaveMapper;

  @Transactional(readOnly = true)
  public Page<LeaveTypeDto> listTypes(Pageable p) {
    return leaveTypeRepository.findAll(p).map(leaveMapper::toDto);
  }

  @Transactional
  public LeaveTypeDto createType(CreateLeaveTypeDto dto) {
    if (leaveTypeRepository.existsByCodeIgnoreCase(dto.getCode()))
      throw new IllegalStateException("type code exists");
    var e = leaveMapper.toEntity(dto);
    return leaveMapper.toDto(leaveTypeRepository.saveAndFlush(e));
  }

  @Transactional
  public LeaveTypeDto updateType(UUID id, UpdateLeaveTypeDto dto) {
    var e =
        leaveTypeRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("type not found"));
    leaveMapper.updateEntity(dto, e);
    leaveTypeRepository.flush();
    return leaveMapper.toDto(e);
  }

  @Transactional
  public void deleteType(UUID id) {
    if (!leaveTypeRepository.existsById(id)) throw new EntityNotFoundException("type not found");
    leaveTypeRepository.deleteById(id);
  }

  // ==== Requests ====
  @Transactional(readOnly = true)
  public LeaveRequestDto get(UUID id) {
    return leaveRequestRepository
        .findById(id)
        .map(leaveMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException("leave request not found"));
  }

  @Transactional
  public LeaveRequestDto create(CreateLeaveRequestDto dto) {
    // базовые проверки
    if (dto.getDateTo().isBefore(dto.getDateFrom()))
      throw new IllegalArgumentException("dateTo < dateFrom");
    // проверь существование типа (и сотрудника, если хочешь дружелюбно)
    leaveTypeRepository
        .findById(dto.getTypeId())
        .orElseThrow(() -> new EntityNotFoundException("type not found"));

    // запрет пересечений с уже одобренными/в работе
    if (!leaveRequestRepository
        .findOverlaps(dto.getEmployeeId(), dto.getDateFrom(), dto.getDateTo())
        .isEmpty()) throw new IllegalStateException("overlapping leave exists");

    // лимит по типу (если задан)
    var type = leaveTypeRepository.getReferenceById(dto.getTypeId());
    if (type.getMaxDaysPerYear() != null && dto.isSubmit()) {
      int year = dto.getDateFrom().getYear();
      int requestedDays =
          (int)
              (java.time.temporal.ChronoUnit.DAYS.between(dto.getDateFrom(), dto.getDateTo()) + 1);
      int approved =
          Optional.ofNullable(
                  leaveRequestRepository.sumApprovedDaysForYear(
                      dto.getEmployeeId(), dto.getTypeId(), year))
              .orElse(0);
      if (approved + requestedDays > type.getMaxDaysPerYear())
        throw new IllegalStateException("yearly limit exceeded");
    }

    var e = leaveMapper.toEntity(dto);
    return leaveMapper.toDto(leaveRequestRepository.save(e));
  }

  @Transactional
  public LeaveRequestDto update(UUID id, UpdateLeaveRequestDto dto) {
    var e =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("leave request not found"));
    if (e.getStatus() != LeaveRequest.Status.DRAFT && e.getStatus() != LeaveRequest.Status.PENDING)
      throw new IllegalStateException("only DRAFT/PENDING can be updated");

    leaveMapper.updateEntity(dto, e);
    if (e.getDateTo().isBefore(e.getDateFrom()))
      throw new IllegalArgumentException("dateTo < dateFrom");

    if (!leaveRequestRepository
        .findOverlaps(e.getEmployeeId(), e.getDateFrom(), e.getDateTo())
        .stream()
        .filter(r -> !r.getId().equals(e.getId()))
        .toList()
        .isEmpty()) throw new IllegalStateException("overlapping leave exists");

    return leaveMapper.toDto(e);
  }

  // Согласование
  @Transactional
  public LeaveRequestDto approve(UUID id, DecisionDto d) {
    var e =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("leave request not found"));
    if (e.getStatus() != LeaveRequest.Status.PENDING)
      throw new IllegalStateException("not PENDING");
    // финальная проверка лимита (если тип с лимитом)
    var type = leaveTypeRepository.getReferenceById(e.getTypeId());
    if (type.getMaxDaysPerYear() != null) {
      int year = e.getDateFrom().getYear();
      int requestedDays =
          (int) (java.time.temporal.ChronoUnit.DAYS.between(e.getDateFrom(), e.getDateTo()) + 1);
      int approved =
          Optional.ofNullable(
                  leaveRequestRepository.sumApprovedDaysForYear(
                      e.getEmployeeId(), e.getTypeId(), year))
              .orElse(0);
      if (approved + requestedDays > type.getMaxDaysPerYear())
        throw new IllegalStateException("yearly limit exceeded");
    }
    e.setApproverId(d.getApproverId());
    if (d.getComment() != null) e.setComment(d.getComment());
    e.setStatus(LeaveRequest.Status.APPROVED);
    return leaveMapper.toDto(e);
  }

  @Transactional
  public LeaveRequestDto reject(UUID id, DecisionDto d) {
    var e =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("leave request not found"));
    if (e.getStatus() != LeaveRequest.Status.PENDING)
      throw new IllegalStateException("not PENDING");
    e.setApproverId(d.getApproverId());
    if (d.getComment() != null) e.setComment(d.getComment());
    e.setStatus(LeaveRequest.Status.REJECTED);
    return leaveMapper.toDto(e);
  }

  @Transactional
  public LeaveRequestDto cancel(UUID id) {
    var e =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("leave request not found"));
    if (e.getStatus() == LeaveRequest.Status.APPROVED
        || e.getStatus() == LeaveRequest.Status.PENDING) {
      e.setStatus(LeaveRequest.Status.CANCELED);
      return leaveMapper.toDto(e);
    }
    throw new IllegalStateException("only PENDING/APPROVED can be canceled");
  }

  // Списки
  @Transactional(readOnly = true)
  public Page<LeaveRequestDto> listByEmployee(UUID empId, Pageable p) {
    return leaveRequestRepository.findByEmployeeId(empId, p).map(leaveMapper::toDto);
  }

  @Transactional(readOnly = true)
  public Page<LeaveRequestDto> listByStatus(LeaveRequest.Status status, Pageable p) {
    return leaveRequestRepository.findByStatus(status, p).map(leaveMapper::toDto);
  }
}
