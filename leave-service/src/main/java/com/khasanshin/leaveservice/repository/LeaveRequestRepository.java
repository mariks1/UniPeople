package com.khasanshin.leaveservice.repository;

import java.time.LocalDate;
import java.util.UUID;

import com.khasanshin.leaveservice.entity.LeaveRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LeaveRequestRepository extends R2dbcRepository<LeaveRequest, UUID> {

  Flux<LeaveRequest> findByEmployeeIdOrderByDateFromDesc(UUID employeeId, Pageable pageable);
  Mono<Long> countByEmployeeId(UUID employeeId);

  Flux<LeaveRequest> findByStatus(LeaveRequest.Status status, Pageable pageable);
  Mono<Long> countByStatus(LeaveRequest.Status status);

  @Query("""
    SELECT EXISTS(
      SELECT 1 FROM leave.leave_request
      WHERE employee_id = :emp
        AND :to >= date_from
        AND date_to >= :from
    )
  """)
  Mono<Boolean> existsOverlaps(@Param("emp") UUID emp,
                               @Param("from") LocalDate from,
                               @Param("to") LocalDate to);

  @Query("""
    SELECT EXISTS(
      SELECT 1 FROM leave.leave_request
      WHERE employee_id = :emp
        AND id <> :id
        AND :to >= date_from
        AND date_to >= :from
    )
  """)
  Mono<Boolean> existsOverlapsExcluding(@Param("id") UUID id,
                                        @Param("emp") UUID emp,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

  // сумма одобренных дней в году (пересекание с рамками года)
  @Query("""
    SELECT COALESCE(SUM(
      LEAST(date_to, make_date(:year,12,31)) - GREATEST(date_from, make_date(:year,1,1)) + 1
    ), 0)
    FROM leave.leave_request
    WHERE employee_id = :emp
      AND type_id = :type
      AND status = 'APPROVED'
      AND date_to   >= make_date(:year,1,1)
      AND date_from <= make_date(:year,12,31)
  """)
  Mono<Integer> sumApprovedDaysForYear(@Param("emp")  UUID emp,
                                       @Param("type") UUID type,
                                       @Param("year") Integer year);
}
