package com.khasanshin.employmentservice.application;

import com.khasanshin.employmentservice.domain.model.Employment;
import com.khasanshin.employmentservice.domain.port.EmployeeVerifierPort;
import com.khasanshin.employmentservice.domain.port.EmploymentRepositoryPort;
import com.khasanshin.employmentservice.domain.port.OrgVerifierPort;
import com.khasanshin.employmentservice.dto.CloseEmploymentDto;
import com.khasanshin.employmentservice.dto.CreateEmploymentDto;
import com.khasanshin.employmentservice.dto.EmploymentDto;
import com.khasanshin.employmentservice.dto.UpdateEmploymentDto;
import com.khasanshin.employmentservice.mapper.EmploymentMapper;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EmploymentApplicationService implements EmploymentUseCase {

    private final EmploymentRepositoryPort repo;
    private final EmploymentMapper mapper;
    private final TransactionTemplate tx;
    private final OrgVerifierPort orgVerifier;
    private final EmployeeVerifierPort employeeVerifier;

    @Override
    public Mono<EmploymentDto> get(UUID id) {
        return Mono.fromCallable(() ->
                        repo.findById(id).orElseThrow(() -> new EntityNotFoundException("employment not found: " + id)))
                .map(mapper::toDto)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<EmploymentDto> create(CreateEmploymentDto dto) {
        return Mono.fromCallable(() -> {
            employeeVerifier.ensureEmployeeExists(dto.getEmployeeId());
            orgVerifier.ensureDepartmentExists(dto.getDepartmentId());
            orgVerifier.ensurePositionExists(dto.getPositionId());

            return tx.execute(status -> {
                var overlaps =
                        repo.findOverlaps(
                                dto.getEmployeeId(),
                                dto.getDepartmentId(),
                                dto.getPositionId(),
                                dto.getStartDate(),
                                null);
                if (!overlaps.isEmpty()) {
                    throw new IllegalStateException("overlapping employment exists for employee/department/position");
                }
                Employment e = mapper.toDomain(dto);
                if (e.getRate() == null) e = e.toBuilder().rate(BigDecimal.valueOf(1.00)).build();
                e = repo.save(e);
                return mapper.toDto(e);
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<EmploymentDto> update(UUID id, UpdateEmploymentDto dto) {
        return Mono.fromCallable(() ->
                        tx.execute(status -> {
                            Employment e =
                                    repo.findById(id)
                                            .orElseThrow(() -> new EntityNotFoundException("employment not found: " + id));

                            Employment updated = mapper.applyUpdates(e, dto);

                            if (updated.getEndDate() != null && updated.getEndDate().isBefore(updated.getStartDate())) {
                                throw new IllegalArgumentException("endDate must be on or after startDate");
                            }

                            updated = repo.save(updated);
                            return mapper.toDto(updated);
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<EmploymentDto> close(UUID id, CloseEmploymentDto body) {
        return Mono.fromCallable(() ->
                        tx.execute(status -> {
                            Employment e = repo.findById(id)
                                    .orElseThrow(() -> new EntityNotFoundException("employment not found: " + id));
                            if (Employment.Status.CLOSED.equals(e.getStatus())) {
                                return mapper.toDto(e);
                            }
                            LocalDate end = body != null && body.getEndDate() != null ? body.getEndDate() : LocalDate.now();
                            if (end.isBefore(e.getStartDate())) {
                                throw new IllegalArgumentException("endDate must be on or after startDate");
                            }
                            Employment closed = e.toBuilder()
                                    .endDate(end)
                                    .status(Employment.Status.CLOSED)
                                    .build();

                            closed = repo.save(closed);
                            return mapper.toDto(closed);
                        }))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<EmploymentDto> listByEmployee(UUID employeeId, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return Mono.fromCallable(() ->
                        repo.findByEmployeeIdOrderByStartDateDesc(employeeId, pageable)
                                .map(mapper::toDto))
                .flatMapMany(pg -> Flux.fromIterable(pg.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> countByEmployee(UUID employeeId) {
        return Mono.fromCallable(() -> repo.countByEmployeeId(employeeId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<EmploymentDto> listByDepartment(UUID departmentId, boolean active, int page, int size) {
        var pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return Mono.fromCallable(() -> {
                    var pg = active
                            ? repo.findByDepartmentIdAndStatus(departmentId, Employment.Status.ACTIVE, pageable)
                            : repo.findByDepartmentId(departmentId, pageable);
                    return pg.map(mapper::toDto);
                })
                .flatMapMany(pg -> Flux.fromIterable(pg.getContent()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> countByDepartment(UUID departmentId, boolean active) {
        return Mono.fromCallable(() ->
                        active
                                ? repo.countByDepartmentIdAndStatus(departmentId, Employment.Status.ACTIVE)
                                : repo.countByDepartmentId(departmentId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
