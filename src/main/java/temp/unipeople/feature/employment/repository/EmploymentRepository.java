package temp.unipeople.feature.employment.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import temp.unipeople.feature.employment.entity.Employment;

public interface EmploymentRepository extends JpaRepository<Employment, UUID> {

  Page<Employment> findByEmployeeIdOrderByStartDateDesc(UUID employeeId, Pageable pageable);

  Page<Employment> findByDepartmentId(UUID departmentId, Pageable pageable);

  Page<Employment> findByDepartmentIdAndStatus(
      UUID departmentId, Employment.Status status, Pageable pageable);

  @Query("""
  select e from Employment e
  where e.employeeId = :emp
    and e.departmentId = :dept
    and e.positionId  = :pos
    and (:endDate   is null or :endDate   >= e.startDate)
    and ( e.endDate is null or  e.endDate >= :startDate)
    """)
  List<Employment> findOverlaps(
      @Param("emp") UUID employeeId,
      @Param("dept") UUID departmentId,
      @Param("pos") UUID positionId,
      @Param("startDate") LocalDate start,
      @Param("endDate") LocalDate end);
}
