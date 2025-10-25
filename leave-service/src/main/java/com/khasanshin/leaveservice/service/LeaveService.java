package com.khasanshin.leaveservice.service;

import com.khasanshin.leaveservice.dto.*;
import com.khasanshin.leaveservice.entity.LeaveRequest;
import com.khasanshin.leaveservice.mapper.LeaveMapper;
import com.khasanshin.leaveservice.repository.LeaveRequestRepository;
import com.khasanshin.leaveservice.repository.LeaveTypeRepository;

import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class LeaveService {

  private final LeaveTypeRepository leaveTypeRepository;
  private final LeaveRequestRepository leaveRequestRepository;
  private final LeaveMapper leaveMapper;

  public Mono<Page<LeaveTypeDto>> listTypes(Pageable p) {
    Mono<Long> total = leaveTypeRepository.count();
    Mono<java.util.List<LeaveTypeDto>> list =
            leaveTypeRepository.findAllBy(p).map(leaveMapper::toDto).collectList();
    return Mono.zip(list, total).map(t -> new PageImpl<>(t.getT1(), p, t.getT2()));
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
    return leaveRequestRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found")))
            .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> create(CreateLeaveRequestDto dto) {
    if (dto.getDateTo().isBefore(dto.getDateFrom()))
      return Mono.error(new IllegalArgumentException("dateTo < dateFrom"));

    return leaveTypeRepository.findById(dto.getTypeId())
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "type not found")))
            .then(leaveRequestRepository.existsOverlaps(dto.getEmployeeId(), dto.getDateFrom(), dto.getDateTo()))
            .flatMap(over -> over
                    ? Mono.error(new IllegalStateException("overlapping leave exists"))
                    : Mono.just(dto))
            .flatMap(d -> {
              if (d.isSubmit()) {
                int year = d.getDateFrom().getYear();
                int requested = (int) (ChronoUnit.DAYS.between(d.getDateFrom(), d.getDateTo()) + 1);
                return leaveRequestRepository.sumApprovedDaysForYear(d.getEmployeeId(), d.getTypeId(), year)
                        .flatMap(approved -> leaveTypeRepository.findById(d.getTypeId())
                                .flatMap(type -> {
                                  Integer limit = type.getMaxDaysPerYear();
                                  if (limit != null && approved + requested > limit) {
                                    return Mono.error(new IllegalStateException("yearly limit exceeded"));
                                  }
                                  return Mono.just(d);
                                }));
              }
              return Mono.just(d);
            })
            .map(leaveMapper::toEntity)
            .flatMap(leaveRequestRepository::save)
            .map(leaveMapper::toDto);

  }

  @Transactional
  public Mono<LeaveRequestDto> update(UUID id, UpdateLeaveRequestDto dto) {
    return leaveRequestRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")))
            .flatMap(e -> {
              if (e.getStatus() != LeaveRequest.Status.DRAFT && e.getStatus() != LeaveRequest.Status.PENDING)
                return Mono.error(new IllegalStateException("only DRAFT/PENDING can be updated"));

              leaveMapper.updateEntity(dto, e);
              if (e.getDateTo().isBefore(e.getDateFrom()))
                return Mono.error(new IllegalArgumentException("dateTo < dateFrom"));

              return leaveRequestRepository.existsOverlapsExcluding(e.getId(), e.getEmployeeId(), e.getDateFrom(), e.getDateTo())
                      .flatMap(over -> over
                              ? Mono.error(new IllegalStateException("overlapping leave exists"))
                              : leaveRequestRepository.save(e));
            })
            .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> approve(UUID id, DecisionDto d) {
    return leaveRequestRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")))
            .flatMap(e -> {
              if (e.getStatus() != LeaveRequest.Status.PENDING)
                return Mono.error(new IllegalStateException("not PENDING"));

              int year = e.getDateFrom().getYear();
              int requested = (int) (java.time.temporal.ChronoUnit.DAYS.between(e.getDateFrom(), e.getDateTo()) + 1);

              return leaveTypeRepository.findById(e.getTypeId())
                      .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")))
                      .flatMap(type -> {
                        Integer limit = type.getMaxDaysPerYear();
                        if (limit == null) return Mono.just(type);
                        return leaveRequestRepository.sumApprovedDaysForYear(e.getEmployeeId(), e.getTypeId(), year)
                                .flatMap(approved -> (approved + requested > limit)
                                        ? Mono.error(new IllegalStateException("yearly limit exceeded"))
                                        : Mono.just(type));
                      })
                      .flatMap(type -> {
                        e.setApproverId(d.getApproverId());
                        if (d.getComment() != null) e.setComment(d.getComment());
                        e.setStatus(LeaveRequest.Status.APPROVED);
                        return leaveRequestRepository.save(e);
                      });
            })
            .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> reject(UUID id, DecisionDto d) {
    return leaveRequestRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")))
            .flatMap(e -> {
              if (e.getStatus() != LeaveRequest.Status.PENDING)
                return Mono.error(new IllegalStateException("not PENDING"));
              e.setApproverId(d.getApproverId());
              if (d.getComment() != null) e.setComment(d.getComment());
              e.setStatus(LeaveRequest.Status.REJECTED);
              return leaveRequestRepository.save(e);
            })
            .map(leaveMapper::toDto);
  }

  @Transactional
  public Mono<LeaveRequestDto> cancel(UUID id) {
    return leaveRequestRepository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "leave request not found")))
            .flatMap(e -> {
              if (e.getStatus() == LeaveRequest.Status.APPROVED || e.getStatus() == LeaveRequest.Status.PENDING) {
                e.setStatus(LeaveRequest.Status.CANCELED);
                return leaveRequestRepository.save(e);
              }
              return Mono.error(new IllegalStateException("only PENDING/APPROVED can be canceled"));
            })
            .map(leaveMapper::toDto);
  }

  public Mono<Page<LeaveRequestDto>> listByEmployee(UUID empId, Pageable p) {
    Mono<Long> total = leaveRequestRepository.countByEmployeeId(empId);
    Mono<java.util.List<LeaveRequestDto>> list =
            leaveRequestRepository.findByEmployeeIdOrderByDateFromDesc(empId, p).map(leaveMapper::toDto).collectList();
    return Mono.zip(list, total).map(t -> new PageImpl<>(t.getT1(), p, t.getT2()));
  }

  public Mono<Page<LeaveRequestDto>> listByStatus(LeaveRequest.Status status, Pageable p) {
    Mono<Long> total = leaveRequestRepository.countByStatus(status);
    Mono<java.util.List<LeaveRequestDto>> list =
            leaveRequestRepository.findByStatus(status, p).map(leaveMapper::toDto).collectList();
    return Mono.zip(list, total).map(t -> new PageImpl<>(t.getT1(), p, t.getT2()));
  }
}
