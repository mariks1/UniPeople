package com.khasanshin.leaveservice.application;

import com.khasanshin.leaveservice.domain.model.LeaveRequest;
import com.khasanshin.leaveservice.domain.model.LeaveType;
import com.khasanshin.leaveservice.domain.port.LeaveRequestRepositoryPort;
import com.khasanshin.leaveservice.domain.port.LeaveTypeRepositoryPort;
import com.khasanshin.leaveservice.dto.CreateLeaveRequestDto;
import com.khasanshin.leaveservice.dto.DecisionDto;
import com.khasanshin.leaveservice.dto.LeaveRequestDto;
import com.khasanshin.leaveservice.dto.UpdateLeaveRequestDto;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
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
public class LeaveRequestApplicationService implements LeaveRequestUseCase {

  private final LeaveTypeRepositoryPort leaveTypeRepository;
  private final LeaveRequestRepositoryPort leaveRequestRepository;
  private final LeaveMapper leaveMapper;

  @Override
  public Mono<LeaveRequestDto> get(UUID id) {
      return getLeaveOr404(id).map(leaveMapper::toDto);
  }

  @Override
  @Transactional
  public Mono<LeaveRequestDto> create(CreateLeaveRequestDto dto) {
      validateDateOrder(dto.getDateFrom(), dto.getDateTo());

      Mono<LeaveType> typeMono = getTypeOr404(dto.getTypeId()).cache();
      Mono<Void> overlaps = requireNoOverlaps(dto.getEmployeeId(), dto.getDateFrom(), dto.getDateTo());

      int year = dto.getDateFrom().getYear();
      int requested = requestedDays(dto.getDateFrom(), dto.getDateTo());
      Mono<Void> limitCheck = dto.isSubmit()
              ? typeMono.flatMap(type -> ensureWithinYearLimit(dto.getEmployeeId(), type, year, requested))
              : Mono.empty();

      return Mono.when(typeMono.then(), overlaps, limitCheck)
              .then(Mono.fromSupplier(() -> leaveMapper.toDomain(dto)))
              .flatMap(leaveRequestRepository::save)
              .map(leaveMapper::toDto);
  }

  @Override
  @Transactional
  public Mono<LeaveRequestDto> update(UUID id, UpdateLeaveRequestDto dto) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "only DRAFT/PENDING can be updated",
                      LeaveRequest.Status.DRAFT, LeaveRequest.Status.PENDING))
              .flatMap(e -> {
                  LeaveRequest updated = leaveMapper.updateLeave(dto, e);
                  validateDateOrder(updated.getDateFrom(), updated.getDateTo());
                  return requireNoOverlapsExcluding(updated.getId(), updated.getEmployeeId(), updated.getDateFrom(), updated.getDateTo())
                          .then(Mono.defer(() -> leaveRequestRepository.save(updated)));
              })
              .map(leaveMapper::toDto);
  }

  @Override
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
                      LeaveRequest updated = e.toBuilder()
                              .approverId(d.getApproverId())
                              .comment(Objects.requireNonNullElse(d.getComment(), e.getComment()))
                              .status(LeaveRequest.Status.APPROVED)
                              .build();
                      return leaveRequestRepository.save(updated);
                  }));
              })
              .map(leaveMapper::toDto);
  }

  @Override
  @Transactional
  public Mono<LeaveRequestDto> reject(UUID id, DecisionDto d) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "not PENDING", LeaveRequest.Status.PENDING))
              .flatMap(e -> {
                  LeaveRequest updated = e.toBuilder()
                          .approverId(d.getApproverId())
                          .comment(Objects.requireNonNullElse(d.getComment(), e.getComment()))
                          .status(LeaveRequest.Status.REJECTED)
                          .build();
                  return leaveRequestRepository.save(updated);
              })
              .map(leaveMapper::toDto);
  }

  @Override
  @Transactional
  public Mono<LeaveRequestDto> cancel(UUID id) {
      return getLeaveOr404(id)
              .flatMap(e -> requireStatus(e, "only PENDING/APPROVED can be canceled",
                      LeaveRequest.Status.PENDING, LeaveRequest.Status.APPROVED))
              .flatMap(e -> leaveRequestRepository.save(e.toBuilder().status(LeaveRequest.Status.CANCELED).build()))
              .map(leaveMapper::toDto);
  }

  @Override
  public Mono<Long> countLeaveByEmployee(UUID empId) {
      return leaveRequestRepository.countByEmployeeId(empId);
  }

  @Override
  public Flux<LeaveRequestDto> listByEmployee(UUID empId, Pageable p) {
      return leaveRequestRepository
              .findByEmployeeIdOrderByDateFromDesc(empId, p)
              .map(leaveMapper::toDto);
  }

  @Override
  public Mono<Long> countLeaveByStatus(LeaveRequest.Status status) {
      return leaveRequestRepository.countByStatus(status);
  }

  @Override
  public Flux<LeaveRequestDto> listByStatus(LeaveRequest.Status status, Pageable p) {
      Pageable capped = PageRequest.of(
              Math.max(0, p.getPageNumber()),
              Math.min(p.getPageSize(), 50),
              p.getSort());
      return leaveRequestRepository.findByStatus(status, capped).map(leaveMapper::toDto);
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
