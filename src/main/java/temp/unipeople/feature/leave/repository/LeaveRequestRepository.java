package temp.unipeople.feature.leave.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import temp.unipeople.feature.leave.entity.LeaveRequest;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

  Page<LeaveRequest> findByEmployeeId(UUID empId, Pageable p);

  Page<LeaveRequest> findByStatus(LeaveRequest.Status status, Pageable p);

  @Query(
      """
    select r from LeaveRequest r
     where r.employeeId = :emp
       and r.status in (temp.unipeople.feature.leave.entity.LeaveRequest.Status.APPROVED,
                        temp.unipeople.feature.leave.entity.LeaveRequest.Status.PENDING)
       and (r.dateFrom <= :to and r.dateTo >= :from)
  """)
  List<LeaveRequest> findOverlaps(
      @Param("emp") UUID emp, @Param("from") LocalDate from, @Param("to") LocalDate to);

  @Query(value = """
  select coalesce(sum((r.date_to - r.date_from + 1)), 0)::int
  from leave_request r
  where r.employee_id = :emp
    and r.type_id     = :type
    and r.status      = 'APPROVED'
    and extract(year from r.date_from) = :year
""", nativeQuery = true)
  int sumApprovedDaysForYear(
          @Param("emp")  UUID emp,
          @Param("type") UUID type,
          @Param("year") int year);

}
