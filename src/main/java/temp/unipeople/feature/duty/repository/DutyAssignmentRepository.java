package temp.unipeople.feature.duty.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import temp.unipeople.feature.duty.entity.DepartmentDutyAssignment;

public interface DutyAssignmentRepository extends JpaRepository<DepartmentDutyAssignment, UUID> {

  Page<DepartmentDutyAssignment> findByDepartmentId(UUID departmentId, Pageable pageable);

  Page<DepartmentDutyAssignment> findByDutyId(UUID dutyId, Pageable pageable);

  boolean existsByDepartmentIdAndEmployeeIdAndDutyId(
      UUID departmentId, UUID employeeId, UUID dutyId);
}
