package com.khasanshin.leaveservice.service;

import com.khasanshin.leaveservice.dto.*;
import com.khasanshin.leaveservice.entity.LeaveRequest;
import com.khasanshin.leaveservice.entity.LeaveType;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
import com.khasanshin.leaveservice.repository.LeaveRequestRepository;
import com.khasanshin.leaveservice.repository.LeaveTypeRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LeaveService {

  private final LeaveTypeRepository leaveTypeRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveMapper leaveMapper;

  public Mono<Long> countTypes() {
      return leaveTypeRepository.count();
  }

    public Flux<LeaveTypeDto> listTypes(Pageable p) {
        Pageable capped = PageRequest.of(
                Math.max(0, p.getPageNumber()),
                Math.min(p.getPageSize(), 50),
                p.getSort()
        );
        return leaveTypeRepository.findAllBy(capped).map(leaveMapper::toDto);
    }
  @Transactional
  public Mono<LeaveTypeDto> createType(CreateLeaveTypeDto dto) {
    return leaveTypeRepository.existsByCodeIgnoreCase(dto.getCode())
            .flatMap(exists -> exists
                    ? Mono.error(new IllegalStateException("type code exists"))
                    : leaveTypeRepository.save(leaveMapper.toEntity(dto)).map(leaveMapper::toDto));
  }

  @Transactional
  public Mono<LeaveTypeDto> updateType(UUID id, UpdateLeaveTypeDto dto) {
    return leaveTypeRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found")))
            .map(e -> { leaveMapper.updateEntity(dto, e); return e; })
            .flatMap(leaveTypeRepository::save)
            .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<Void> deleteType(UUID id) {
    return leaveTypeRepository.existsById(id)
            .flatMap(exists -> exists
                    ? leaveTypeRepository.deleteById(id)
                    : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found")));
  }

  public Mono<LeaveRequestDto> get(UUID id) {
      return getLeaveOr404(id).map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> create(CreateLeaveRequestDto dto) { // TODO облегчить
      validateDateOrder(dto.getDateFrom(), dto.getDateTo());

      Mono<LeaveType> typeMono = getTypeOr404(dto.getTypeId()).cache();
      Mono<Void> overlaps = requireNoOverlaps(dto.getEmployeeId(), dto.getDateFrom(), dto.getDateTo());

      int year = dto.getDateFrom().getYear();
      int requested = requestedDays(dto.getDateFrom(), dto.getDateTo());
      Mono<Void> limitCheck = dto.isSubmit()
              ? typeMono.flatMap(type -> ensureWithinYearLimit(dto.getEmployeeId(), type, year, requested))
              : Mono.empty();

      return Mono.when(typeMono.then(), overlaps, limitCheck)
              .then(Mono.fromSupplier(() -> leaveMapper.toEntity(dto)))
              .flatMap(leaveRequestRepository::save)
              .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> update(UUID id, UpdateLeaveRequestDto dto) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "only DRAFT/PENDING can be updated",
                      LeaveRequest.Status.DRAFT, LeaveRequest.Status.PENDING))
              .flatMap(e -> {
                  leaveMapper.updateEntity(dto, e);
                  validateDateOrder(e.getDateFrom(), e.getDateTo());
                  return requireNoOverlapsExcluding(e.getId(), e.getEmployeeId(), e.getDateFrom(), e.getDateTo())
                          .then(leaveRequestRepository.save(e));
              })
              .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> approve(UUID id, DecisionDto d) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "not PENDING", LeaveRequest.Status.PENDING))
              .flatMap(e -> {
                  int year = e.getDateFrom().getYear();
                  int requested = requestedDays(e.getDateFrom(), e.getDateTo());

                  Mono<Void> limitCheck = getTypeOr404(e.getTypeId())
                          .flatMap(type -> ensureWithinYearLimit(e.getEmployeeId(), type, year, requested));

                  return limitCheck.then(Mono.defer(() -> {
                      e.setApproverId(d.getApproverId());
                      if (Objects.nonNull(d.getComment())) e.setComment(d.getComment());
                      e.setStatus(LeaveRequest.Status.APPROVED);
                      return leaveRequestRepository.save(e);
                  }));
              })
              .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> reject(UUID id, DecisionDto d) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "not PENDING", LeaveRequest.Status.PENDING))
              .flatMap(e -> {
                  e.setApproverId(d.getApproverId());
                  if (Objects.nonNull(d.getComment())) e.setComment(d.getComment());
                  e.setStatus(LeaveRequest.Status.REJECTED);
                  return leaveRequestRepository.save(e);
              })
              .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> cancel(UUID id) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "only PENDING/APPROVED can be canceled",
                      LeaveRequest.Status.PENDING, LeaveRequest.Status.APPROVED))
              .flatMap(e -> {
                  e.setStatus(LeaveRequest.Status.CANCELED);
                  return leaveRequestRepository.save(e);
              })
              .map(leaveMapper::toDto);
  }

  public Mono<Long> countLeaveByEmployee(UUID empId) {
      return leaveRequestRepository.countByEmployeeId(empId);
  }

  public Flux<LeaveRequestDto> listByEmployee(UUID empId, Pageable p) {
      return leaveRequestRepository
              .findByEmployeeIdOrderByDateFromDesc(empId, p)
              .map(leaveMapper::toDto);
  }

  public Mono<Long> countLeaveByStatus(LeaveRequest.Status status) {
      return leaveRequestRepository.countByStatus(status);
  }

  public Flux<LeaveRequestDto> listByStatus(LeaveRequest.Status status, Pageable p) {
      return leaveRequestRepository.findByStatus(status, p).map(leaveMapper::toDto);
  }

    private Mono<LeaveType> getTypeOr404(UUID typeId) {
        return leaveTypeRepository.findById(typeId).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found")));
    }

    private Mono<LeaveRequest> getLeaveOr404(UUID id) {
        return leaveRequestRepository.findById(id).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")));
    }

    private void validateDateOrder(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) throw new IllegalArgumentException("dateTo < dateFrom");
    }

    private int requestedDays(LocalDate from, LocalDate to) {
        return (int) (ChronoUnit.DAYS.between(from, to) + 1);
    }

    private Mono<Void> requireNoOverlaps(UUID employeeId, LocalDate from, LocalDate to) {
        return leaveRequestRepository.existsOverlaps(employeeId, from, to)
                .flatMap(over -> over
                        ? Mono.error(new IllegalStateException("overlapping leave exists"))
                        : Mono.empty());
    }

    private Mono<Void> requireNoOverlapsExcluding(UUID id, UUID employeeId, LocalDate from, LocalDate to) {
        return leaveRequestRepository.existsOverlapsExcluding(id, employeeId, from, to)
                .flatMap(over -> over
                        ? Mono.error(new IllegalStateException("overlapping leave exists"))
                        : Mono.empty());
    }

    private Mono<LeaveRequest> requireStatus(LeaveRequest e, String onErrorMessage, LeaveRequest.Status... allowed) {
        EnumSet<LeaveRequest.Status> set = EnumSet.noneOf(LeaveRequest.Status.class);
        Collections.addAll(set, allowed);
        return set.contains(e.getStatus())
                ? Mono.just(e)
                : Mono.error(new IllegalStateException(onErrorMessage));
    }

    private Mono<Void> ensureWithinYearLimit(UUID employeeId, LeaveType type, int year, int requested) {
        Integer limit = type.getMaxDaysPerYear();
        if (limit == null) return Mono.empty();

        return leaveRequestRepository
                .sumApprovedDaysForYear(employeeId, type.getId(), year)
                .flatMap(approved -> (approved + requested > limit)
                        ? Mono.error(new IllegalStateException("yearly limit exceeded"))
                        : Mono.empty());
    }
}
